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

//Messages Activity
class MessagesActivity : Fragment() {
    private lateinit var binding: MessagesActivityBinding

    //ViewModel
    val viewModel : MessagesViewModel by activityViewModels()

    //Recycler view
    private lateinit var recyclerView : RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    //Shared preferences object
    private lateinit var loginSharedPreferences: SharedPreferences

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MessagesActivityBinding.inflate(layoutInflater)

        //Initialising the recycler view object
        recyclerView = binding.messagesList
        layoutManager = LinearLayoutManager(this.activity)
        recyclerView.layoutManager = layoutManager

        //Initialising the shared preferences object
        loginSharedPreferences = requireActivity().getSharedPreferences(getString(R.string.login_preferences_name),
            Context.MODE_PRIVATE)

        //Getting args from the previous activity provided to it
        val args = MessagesActivityArgs.fromBundle(requireArguments())

        //Getting the auth_token of the current user from the args
        val auth_token = args.authToken

        //Setting the token in the viewmodel
        viewModel.setToken(auth_token)

        //Creating an adapter for the messages adapter with clicklistener
        val adapter = MessagesAdapter(MessageClickListener {
//            Toast.makeText(this.activity, "${it.customerId} clicked", Toast.LENGTH_SHORT).show()
            //Navigating to conversation activity on click
            val action = MessagesActivityDirections.actionMessagesActivity2ToConversationActivity2(it.customerId)
            requireView().findNavController().navigate(action)
        })

        //Putting adapter to the recycler view
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                (layoutManager as LinearLayoutManager).orientation
            )
        )

        //Viewmodel function call to get messages
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

        //Observing threads boolean to see when the http request is completed
        viewModel.threads.observe(viewLifecycleOwner) {
            adapter.submitList(viewModel.getActiveThreads())
        }

//        println("Token received -> ${args.authToken}")

        //Setting the menu to be true
        setHasOptionsMenu(true)

        return binding.root
    }

    //Inflating menu object for the current fragment
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.messages_activity_menu, menu)
    }

    //Calling functions on clicking on the items in the menu
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_logout_button -> {
                //Logging out when logout item is clicked
                logOut()
                true
            }
            R.id.menu_reset_button -> {
                //Calling the reset request when reset item is clicked
                viewModel.reset()
                viewModel.getMessages()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Function to logout the current user
    private fun logOut(){
        //Deleting the data of the current user from the shared preferences
        loginSharedPreferences.edit().putBoolean(getString(R.string.logIn_bool), false)
            .apply()
        loginSharedPreferences.edit()
            .putString(getString(R.string.logIn_token), "").apply()

        //Going back to the login activity
        requireView().findNavController().popBackStack()
    }
}