package com.example.bin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView

class RequestAdapter(private val requests: List<Request>): RecyclerView.Adapter
    <RequestAdapter.RequestHolder>() {
    var som: String = ""
    class RequestHolder(v: View): RecyclerView.ViewHolder(v) {
        val view = v

        fun addRequestRecord(request: Request) {
            view.findViewById<TextView>(R.id.binTextView).text = request.bin
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestHolder {
        return RequestHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item_row, parent, false))
    }

    override fun onBindViewHolder(holder: RequestHolder, position: Int) {
        holder.addRequestRecord(requests[position])
        holder.itemView.setOnClickListener{
            val activity = it.context as AppCompatActivity
            activity.intent.putExtra("binNumberCompleted", requests[position].bin)
            holder.view.findNavController().navigate(R.id.action_historyFragment_to_infoFragment)
        }
    }

    override fun getItemCount(): Int {
        return requests.count()
    }

}