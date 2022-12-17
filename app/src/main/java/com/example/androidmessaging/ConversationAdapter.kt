package com.example.androidmessaging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidmessaging.databinding.ConversationListViewBinding
import com.example.androidmessaging.databinding.MessageListViewBinding

class ConversationAdapter() : ListAdapter<MessageThread, ConversationAdapter.ViewHolder>(MessagesDiffCallBack()) {
    //Function returning viewholder after creating it
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    //Function to bind the views to the recycler view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    //Viewholder class
    class ViewHolder(val binding : ConversationListViewBinding) : RecyclerView.ViewHolder(binding.root){
        val res = itemView.context.resources

        //Function binding the layout of the viewholder with the data
        fun bind(item : MessageThread){
            val timeString = "${item.timestamp.subSequence(0, 10)} : ${item.timestamp.subSequence(11, 19)}"
            val agent = "User Id : ${
                if(item.agentId == "-1"){
                    item.customerId
                } else {
                    item.agentId
                }
            }"
            binding.descriptionMessage.text = item.body
            binding.agent.text = agent
            binding.datestamp.text = timeString
        }

        //Function creating the viewholder item
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ConversationListViewBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

}