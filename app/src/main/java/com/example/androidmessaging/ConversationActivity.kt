package com.example.androidmessaging

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidmessaging.databinding.ConversationActivityBinding
import com.example.androidmessaging.viewmodels.MessagesViewModel

//Conversation activity for individual thread
class ConversationActivity : Fragment() {
    //ViewModel
    val viewModel : MessagesViewModel by  activityViewModels()

    //Layout objects of the conversation activity
    private lateinit var recyclerView : RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var messageTextView : EditText
    private lateinit var sendButton : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ConversationActivityBinding.inflate(layoutInflater)

        //Getting the arguments passed to this fragment
        val args = ConversationActivityArgs.fromBundle(requireArguments())

        //Getting the customer id out of the args passed
        val customerId = args.customerId

        //Initialising the views with the binding
        recyclerView = binding.conversationView
        layoutManager = LinearLayoutManager(this.activity)
        recyclerView.layoutManager = layoutManager

        messageTextView = binding.sendMessageText
        sendButton = binding.sendButton

        //Declaring the adapter for Conversations
        val adapter = ConversationAdapter()

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                (layoutManager as LinearLayoutManager).orientation
            )
        )

        //Getting customer messages from the viewModel
        val list = viewModel.getDataForCustomer(customerId)

        //Submitting the list to the adapter
        adapter.submitList(list)

        //Implementing the clicklistener and sending the message when send button is clicked
        sendButton.setOnClickListener {
            //Getting the message body
            var body = messageTextView.text.toString()

            //Checking if the message body is empty
            if(body.isNullOrBlank()){
                return@setOnClickListener
            }

            //Trimming the message of any extra white spaces
            body = body.trim()

            //Getting the current thread id
            val threadId = list.last().threadId

            //Calling the viewmodel function to send the message
            viewModel.sendMessage(body, threadId)
        }

        //Observing the messageSent boolean and the performing operations when message is sent
        viewModel.messageSent.observe(viewLifecycleOwner) {
            //Getting the list of messages and updating the recycler view adapter with the ew message sent
            val newList = viewModel.getDataForCustomer(customerId)
            adapter.notifyItemInserted(newList.size-1)
            messageTextView.text.clear()
        }

        return binding.root
    }
}