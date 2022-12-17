package com.example.androidmessaging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidmessaging.databinding.MessageListViewBinding

//Messages adapter for messages activity
class MessagesAdapter(val clickListener : MessageClickListener) : ListAdapter<MessageThread, MessagesAdapter.ViewHolder>(MessagesDiffCallBack()) {
    //Function returning viewholder after creating it
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    //Function to bind the views to the recycler view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position),clickListener)
    }

    //Viewholder class
    class ViewHolder(val binding : MessageListViewBinding) : RecyclerView.ViewHolder(binding.root){
        val res = itemView.context.resources

        //Function binding the layout of the viewholder with the data
        fun bind(item : MessageThread,clickListener: MessageClickListener){
            binding.descriptionMessage.text = if(item.body.length > 25){
                "${item.body.subSequence(0, 25)}..."
            } else {
                item.body
            }
            val agent = "User Id : ${
                if(item.agentId == "-1"){
                    item.customerId
                } else {
                    item.agentId
                }
            }"
            binding.agent.text = agent
            binding.datestamp.text = item.timestamp.subSequence(0, 10)
            binding.timestamp.text = item.timestamp.subSequence(11, 19)

            //Click listener for when the object is clicked
            binding.messageListViewRoot.setOnClickListener{
                clickListener.onClick(item)
            }
        }

        //Function creating the viewholder item
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MessageListViewBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

}

//Diffcallback method to reload the item which is changed
class MessagesDiffCallBack : DiffUtil.ItemCallback<MessageThread>() {
    override fun areItemsTheSame(oldItem: MessageThread, newItem: MessageThread): Boolean {
        return oldItem.messageId == newItem.messageId
    }

    override fun areContentsTheSame(oldItem: MessageThread, newItem: MessageThread): Boolean {
        return oldItem == newItem
    }

}

//Click listener class for the item
class MessageClickListener(val clickListener : (currentMessage : MessageThread) -> Unit){
    fun onClick(currentMessage : MessageThread) = clickListener(currentMessage)
}