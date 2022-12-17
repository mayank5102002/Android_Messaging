package com.example.androidmessaging

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.androidmessaging.databinding.LoginActivityBinding
import com.example.androidmessaging.viewmodels.loginViewModel

//Login activity
class LoginActivity : Fragment() {
    private lateinit var binding: LoginActivityBinding

    //ViewModel
    private val viewModel : loginViewModel by activityViewModels()

    //Shared Preferences object
    private lateinit var loginSharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginActivityBinding.inflate(layoutInflater)

        //Calling viewmodel init function
        viewModel.init()

        //Initialising the shared preferences object
        loginSharedPreferences = requireActivity().getSharedPreferences(getString(R.string.login_preferences_name),
            Context.MODE_PRIVATE)

        //Assigning a listener to the login button for when it is clicked
        binding.loginButton.setOnClickListener{
            verify()
        }

        //Observing the process boolean for when the user gets logged In
        viewModel.process.observe(viewLifecycleOwner) {
            if (it) {
                //Checking if the user is logged in
                if (viewModel.loginAuthorised.value == true) {
//                    println("Logged in -> ${viewModel.loginAuthorised.value}")
                    val token = viewModel.authToken.value

                    //Putting the auth_token in the shared preferences to get the login data next time
                    loginSharedPreferences.edit().putBoolean(getString(R.string.logIn_bool), true)
                        .apply()
                    loginSharedPreferences.edit()
                        .putString(getString(R.string.logIn_token), token!!).apply()

//                    println(
//                        "Token saving -> ${
//                            loginSharedPreferences.getString(
//                                getString(R.string.logIn_token),
//                                null
//                            )
//                        }"
//                    )

                    //Navigating to messages activity
                    navigateToMessages(token)
                } else {
                    //Creating error alert if user isn't logged in
                    createError()
                }

                //Starting the process for viewmodel again
                viewModel.processStart()
            }
        }

        return binding.root
    }

    //Checking if the user is already logged in from any past activity
    private fun checkAlreadyLoggedIn() : Boolean {
        //Getting logged in boolean from preferences
        val loggedIn = loginSharedPreferences.getBoolean(getString(R.string.logIn_bool), false)

        if(loggedIn){
            //Getting user token from preferences
            val token = loginSharedPreferences.getString(getString(R.string.logIn_token), null)
            if(!token.isNullOrBlank()){
                println("Token saved -> $token")

                //Updating the auth_token in viewmodel and going to messages activity
                viewModel.loggedIn(token)

                return true
            }
        }

        return false
    }

    //Function to navigate to messages activity
    private fun navigateToMessages(token : String){
        binding.username.text.clear()
        binding.password.text.clear()

        val action = LoginActivityDirections.actionLoginActivity2ToMessagesActivity2(token)
        requireView().findNavController().navigate(action)
    }

    //Function to verify when the login button is clicked
    private fun verify() {
        viewModel.init()

        val username = binding.username.text.toString()
        val password = binding.password.text.toString()

        if(username.isBlank() || password.isBlank() || username != password.reversed()){
            if(!checkAlreadyLoggedIn()){
                //If there is already a user then logIn else create error
                createError()
            }
            return
        }

        //Logging in using viewmodel function
        viewModel.logIn(username, password)
    }

    //Creating error alert for invalid details
    private fun createError() {
        activity?.let {
            //Creating alert dialog for signifying invalid details
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.errorTitle)
                setNegativeButton(R.string.removeError
                ) { dialog, id ->
                    binding.username.text.clear()
                    binding.password.text.clear()
                }
            }
            builder.create()
        }?.show()
    }
}