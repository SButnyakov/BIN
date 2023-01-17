package com.example.bin

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface BinlistAPI {
    @GET("{bin}")
    fun searchCard(@Path("bin") bin: String): Call<Card>

}

class Card(
    val number: CardNumber? = null, val scheme: String? = null, val type: String? = null,
    val brand: String? = null, val prepaid: Boolean? = null, val country: CardCountry? = null,
    val bank: CardBank? = null)

class CardNumber(val length: String? = null, val luhn: Boolean? = null)

class CardCountry(
    var name: String? = null, val emoji: String? = null, val currency: String? = null)

class CardBank(
    val name: String? = null, val url: String? = null, var phone: String? = null,
    var city: String? = null)

class CardRetriever {
     private var service: BinlistAPI

     init {
         val retrofit = Retrofit
             .Builder()
             .baseUrl("https://lookup.binlist.net/")
             .addConverterFactory(GsonConverterFactory.create())
             .build()
         service = retrofit.create(BinlistAPI::class.java)
     }

    fun searchCard(callback: Callback<Card>, bin: String) {
        val call = service.searchCard(bin)
        call.enqueue(callback)
    }
}
