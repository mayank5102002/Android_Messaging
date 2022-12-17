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

class ConversationActivity : Fragment() {
    val viewModel : MessagesViewModel by  activityViewModels()
    private lateinit var recyclerView : RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var messageTextView : EditText
    private lateinit var sendButton : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ConversationActivityBinding.inflate(layoutInflater)

        val args = ConversationActivityArgs.fromBundle(requireArguments())

        val customerId = args.customerId

        recyclerView = binding.conversationView
        layoutManager = LinearLayoutManager(this.activity)
        recyclerView.layoutManager = layoutManager

        messageTextView = binding.sendMessageText
        sendButton = binding.sendButton

        val adapter = ConversationAdapter()

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                (layoutManager as LinearLayoutManager).orientation
            )
        )

        val list = viewModel.getDataForCustomer(customerId)

        adapter.submitList(list)

        sendButton.setOnClickListener {
            var body = messageTextView.text.toString()

            if(body.isNullOrBlank()){
                return@setOnClickListener
            }

            body = body.trim()

            val threadId = list.last().threadId

            viewModel.sendMessage(body, threadId)
        }

        viewModel.messageSent.observe(viewLifecycleOwner) {
            val newList = viewModel.getDataForCustomer(customerId)
            adapter.notifyItemInserted(newList.size-1)
            messageTextView.text.clear()
        }

        return binding.root
    }
}