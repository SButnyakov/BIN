package com.example.bin

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bin.databinding.FragmentInfoBinding
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InfoFragment: Fragment() {
    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cardCompleted = activity?.intent?.extras?.get("binNumberCompleted")
        if (cardCompleted != null) {
            activity?.intent?.removeExtra("binNumberCompleted")
            retrieveCardInfoFromRealm(cardCompleted as String)
        } else {
            retrieveCardInfoFromAPI(activity?.intent?.extras?.get("binNumber") as String)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.scrollView.visibility = View.VISIBLE
        binding.cardNotFoundTextView.visibility = View.GONE
        binding.connectionFailureTextView.visibility = View.GONE
        _binding = null
    }

    private fun retrieveCardInfoFromRealm(bin: String) {
        val config = RealmConfiguration.create(schema = setOf(Request::class))
        val realm: Realm = Realm.open(config)

        val request = realm.query<Request>("bin == $0", bin).first().find()

        val card = Card(
            CardNumber(request?.numberLength, request?.numberLuhn), request?.scheme,
            request?.type, request?.brand, request?.prepaid, CardCountry(request?.countryName,
                request?.countryEmoji, request?.countryCurrency), CardBank(request?.bankName,
                request?.bankUrl, request?.bankPhone, request?.bankCity))

        fillTable(card)

        realm.writeBlocking {
            findLatest(request!!)?.let { delete(it) }

            copyToRealm(Request().apply {
                this.bin = bin
                scheme = card.scheme
                type = card.type
                brand = card.brand
                prepaid = card.prepaid
                numberLength = card.number?.length
                numberLuhn = card.number?.luhn
                countryName = card.country?.name
                countryEmoji = card.country?.emoji
                countryCurrency = card.country?.currency
                bankName = card.bank?.name
                bankUrl = card.bank?.url
                bankPhone = card.bank?.phone
                bankCity = card.bank?.city
            })
        }
    }

    private fun retrieveCardInfoFromAPI(bin: String) {
        val retriever = CardRetriever()

        val callback = object: Callback<Card> {
            override fun onResponse(call: Call<Card>, response: Response<Card>) {
                if (response.body() == null) {
                    binding.scrollView.visibility = View.GONE
                    binding.cardNotFoundTextView.visibility = View.VISIBLE
                    return
                } else {
                    fillTable(response.body()!!)

                    val config = RealmConfiguration.create(schema = setOf(Request::class))
                    val realm: Realm = Realm.open(config)

                    realm.writeBlocking {
                        val request = realm.query<Request>("bin == $0", bin).first().find()

                        if (request != null) {
                            findLatest(request)?.let { delete(it) }
                        }

                        copyToRealm(Request().apply {
                            val card = response.body()!!
                            this.bin = bin
                            scheme = card.scheme
                            type = card.type
                            brand = card.brand
                            prepaid = card.prepaid
                            numberLength = card.number?.length
                            numberLuhn = card.number?.luhn
                            countryName = card.country?.name
                            countryEmoji = card.country?.emoji
                            countryCurrency = card.country?.currency
                            bankName = card.bank?.name
                            bankUrl = card.bank?.url
                            bankPhone = card.bank?.phone
                            bankCity = card.bank?.city
                        })
                    }
                }
            }

            override fun onFailure(call: Call<Card>, t: Throwable) {
                binding.scrollView.visibility = View.GONE
                binding.connectionFailureTextView.visibility = View.VISIBLE
            }

        }

        retriever.searchCard(callback, bin)
    }

    fun fillTable(card: Card) {
        setValue(card.scheme?.replaceFirstChar { it.uppercase() }, binding.cardSchemeValueTextView)
        setDebitOrCredit(card.type, binding.typeDebitTextView, binding.typeCreditTextView)
        setValue(card.brand, binding.cardBrandValueTextView)
        setYesOrNo(card.prepaid, binding.prepaidYesTextView, binding.prepaidNoTextView)
        setValue(card.number?.length, binding.cardLengthValueTextView)
        setYesOrNo(card.number?.luhn, binding.cardNumberLuhnYesTextView, binding.cardLuhnNoTextView)
        setValue(card.country?.emoji, binding.countryEmojiValueTextView)

        if (card.country?.name != null) {
            val spanString = SpannableString(card.country.name)
            spanString.setSpan(UnderlineSpan(), 0, spanString.length, 0)
            binding.countryNameValueTextView.text = spanString
            binding.countryNameValueTextView.setTextColor(Color.parseColor("#ff0099cc"))
            binding.countryNameValueTextView.setOnClickListener {
                startMap(card.country.name!!)
            }
        }

        setValue(card.country?.currency, binding.countryCurrencyValueTextView)
        setValue(card.bank?.name, binding.bankNameValueTextView)

        if (card.bank?.city != null) {
            val spanString = SpannableString(card.bank.city)
            spanString.setSpan(UnderlineSpan(), 0, spanString.length, 0)
            binding.bankCityValueTextView.text = spanString
            binding.bankCityValueTextView.setTextColor(Color.parseColor("#ff0099cc"))
            binding.bankCityValueTextView.setOnClickListener {
                startMap(card.bank.city!!)
            }
        }

        setValue(card.bank?.url, binding.bankUrlValueTextView)

        if (card.bank?.phone != null) {
            card.bank.phone = clearPhoneNumber(card.bank.phone!!)
        }

        setValue(card.bank?.phone, binding.bankPhoneValueTextView)
        moveLayout(card.scheme, card.type, binding.cardSchemeLayout, binding.cardTypeLayout,
            binding.cardSchemeTypeRow)
        moveLayout(card.brand, card.prepaid, binding.cardBrandLayout, binding.cardPrepaidLayout,
            binding.cardBrandPrepaidRow)

        if (card.number == null) {
            binding.cardValuesRow.visibility = View.GONE
        } else {
            moveLayout(card.number.length, card.number.luhn, binding.cardLengthLayout,
                binding.cardLuhnLayout, binding.cardValuesRow)
        }
        if (card.scheme == null && card.type == null && card.brand == null
            && card.prepaid == null &&
            (card.number == null || card.number.length == null && card.number.luhn == null)) {
            binding.cardLabelRow.visibility = View.GONE
            binding.countryDivider.visibility = View.GONE
        }

        if (card.country == null) {
            binding.countryValuesRow.visibility = View.GONE
        } else {
            moveLayout(card.country.name, card.country.currency, binding.countryNameLayout,
                binding.countryCurrenctLayout, binding.countryValuesRow)
        }
        if (card.country == null || card.country.name == null && card.country.currency == null) {
            binding.countryRow.visibility = View.GONE
            binding.bankDivider.visibility = View.GONE
        }

        if (card.bank == null) {
            binding.bankNameCityRow.visibility = View.GONE
            binding.bankUrlPhoneRow.visibility = View.GONE
        } else {
            moveLayout(card.bank.name, card.bank.city, binding.bankNameLayout,
                binding.bankCityLayout, binding.bankNameCityRow)
            moveLayout(card.bank.url, card.bank.phone, binding.bankUrlLayout,
                binding.bankPhoneLayout, binding.bankUrlPhoneRow)
        }

        if (card.bank == null || card.bank.name == null && card.bank.city == null
            && card.bank.url == null && card.bank.phone == null) {
            binding.bankLabelRow.visibility = View.GONE
        }
    }

    private fun setValue(value: String?, textView: TextView) {
        textView.text = value ?: return
    }

    private fun setDebitOrCredit(response: String?, textViewDebit: TextView, textViewCredit: TextView) {
        if (response == null) return
        when (response) {
            "debit" -> {
                textViewDebit.setTextColor(Color.BLACK)
                textViewDebit.typeface = Typeface.DEFAULT_BOLD
            }
            "credit" -> {
                textViewCredit.setTextColor(Color.BLACK)
                textViewCredit.typeface = Typeface.DEFAULT_BOLD
            }
            else -> return
        }
    }

    private fun setYesOrNo(response: Boolean?, textViewYes: TextView, textViewNo: TextView) {
        if (response == null) return
        when (response) {
            true -> {
                textViewYes.setTextColor(Color.BLACK)
                textViewYes.typeface = Typeface.DEFAULT_BOLD
            }
            false -> {
                textViewNo.setTextColor(Color.BLACK)
                textViewNo.typeface = Typeface.DEFAULT_BOLD
            }
        }
    }

    private fun clearPhoneNumber(phone: String): String {
        var number = phone.replace(Regex("[^0-9]"), "")
        number = "+$number"
        return number
    }

    private fun startMap(place: String) {
        val uri = "https://www.google.com/maps/search/?api=1&query=${place}"
        val mapAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        mapAppIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapAppIntent)
    }

    private fun moveLayout(
        firstElement: Any?, secondElement: Any?,
        firstLayout: LinearLayout, secondLayout: LinearLayout,
        row: TableRow) {
        if (firstElement == null && secondElement != null) {
            firstLayout.visibility = View.GONE
            row.gravity = Gravity.LEFT
        } else if (firstElement != null && secondElement == null) {
            secondLayout.visibility = View.GONE
            row.gravity = Gravity.LEFT
        } else if (firstElement == null && secondElement == null) {
            row.visibility = View.GONE
        }
    }
}