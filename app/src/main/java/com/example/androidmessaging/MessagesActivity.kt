package com.example.androidmessaging

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidmessaging.databinding.LoginActivityBinding
import com.example.androidmessaging.databinding.MessagesActivityBinding
import com.example.androidmessaging.viewmodels.MessagesViewModel

class MessagesActivity : Fragment() {
    private lateinit var binding: MessagesActivityBinding
    val viewModel : MessagesViewModel by activityViewModels()
    private lateinit var recyclerView : RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var loginSharedPreferences: SharedPreferences

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MessagesActivityBinding.inflate(layoutInflater)

        recyclerView = binding.messagesList
        layoutManager = LinearLayoutManager(this.activity)
        recyclerView.layoutManager = layoutManager

        loginSharedPreferences = requireActivity().getSharedPreferences(getString(R.string.login_preferences_name),
            Context.MODE_PRIVATE)

        val args = MessagesActivityArgs.fromBundle(requireArguments())

        val auth_token = args.authToken

        viewModel.setToken(auth_token)

        val adapter = MessagesAdapter(MessageClickListener {
//            Toast.makeText(this.activity, "${it.customerId} clicked", Toast.LENGTH_SHORT).show()
            val action = MessagesActivityDirections.actionMessagesActivity2ToConversationActivity2(it.customerId)
            requireView().findNavController().navigate(action)
        })

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                (layoutManager as LinearLayoutManager).orientation
            )
        )

        viewModel.getMessages()

//        val list = MutableList(5){
//            MessageThread(5, 5, "22", 5, "fasfasf", "safasffa")
//            MessageThread(5, 5, "22", 5, "fasfasf", "safasffa")
//            MessageThread(5, 5, "22", 5, "fasfasf", "safasffa")
//            MessageThread(5, 5, "22", 5, "fasfasf", "safasffa")
//            MessageThread(5, 5, "22", 5, "fasfasf", "safasffa")
//        }
//
//        adapter.submitList(list)
//        adapter.notifyDataSetChanged()

        viewModel.threads.observe(viewLifecycleOwner) {
            adapter.submitList(viewModel.getActiveThreads())
        }

//        println("Token received -> ${args.authToken}")

        setHasOptionsMenu(true)

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.messages_activity_menu, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_logout_button -> {
                logOut()
                true
            }
            R.id.menu_reset_button -> {
                viewModel.reset()
                viewModel.getMessages()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logOut(){
        loginSharedPreferences.edit().putBoolean(getString(R.string.logIn_bool), false)
            .apply()
        loginSharedPreferences.edit()
            .putString(getString(R.string.logIn_token), "").apply()

        requireView().findNavController().popBackStack()
    }
}