package com.example.bin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bin.databinding.FragmentHistoryBinding
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults

class HistoryFragment: Fragment() {
    private var _binding: FragmentHistoryBinding? = null

    private val binding get() = _binding!!

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: RequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateRecycler()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.visibility = View.GONE
        binding.noRecordsFoundTextView.visibility = View.GONE
        _binding = null
    }

    private fun updateRecycler() {
        val config = RealmConfiguration.create(schema = setOf(Request::class))
        val realm: Realm = Realm.open(config)
        val requests: RealmResults<Request> = realm.query<Request>().find()

        layoutManager = LinearLayoutManager(activity)
        adapter = RequestAdapter(requests.toList().asReversed())

        if (requests.isEmpty()) {
            binding.noRecordsFoundTextView.visibility = View.VISIBLE
        } else {
            binding.recyclerView.layoutManager = layoutManager
            binding.recyclerView.adapter = adapter
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
}
