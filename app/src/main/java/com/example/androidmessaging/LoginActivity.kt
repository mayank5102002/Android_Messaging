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

class LoginActivity : Fragment() {
    private lateinit var binding: LoginActivityBinding
    private val viewModel : loginViewModel by activityViewModels()

    private lateinit var loginSharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginActivityBinding.inflate(layoutInflater)

        viewModel.init()

        loginSharedPreferences = requireActivity().getSharedPreferences(getString(R.string.login_preferences_name),
            Context.MODE_PRIVATE)

        binding.loginButton.setOnClickListener{
            verify()
        }

        viewModel.process.observe(viewLifecycleOwner) {
            if (it) {
                if (viewModel.loginAuthorised.value == true) {
//                    println("Logged in -> ${viewModel.loginAuthorised.value}")
                    val token = viewModel.authToken.value

                    loginSharedPreferences.edit().putBoolean(getString(R.string.logIn_bool), true)
                        .apply()
                    loginSharedPreferences.edit()
                        .putString(getString(R.string.logIn_token), token!!).apply()

                    println(
                        "Token saving -> ${
                            loginSharedPreferences.getString(
                                getString(R.string.logIn_token),
                                null
                            )
                        }"
                    )

                    navigateToMessages(token)
                } else {
                    createError()
                }

                viewModel.processStart()
            }
        }

        return binding.root
    }

    private fun checkAlreadyLoggedIn() : Boolean {
        val loggedIn = loginSharedPreferences.getBoolean(getString(R.string.logIn_bool), false)

        if(loggedIn){
            val token = loginSharedPreferences.getString(getString(R.string.logIn_token), null)
            if(!token.isNullOrBlank()){
                println("Token saved -> $token")

                viewModel.loggedIn(token)

                return true
            }
        }

        return false
    }

    private fun navigateToMessages(token : String){
        val action = LoginActivityDirections.actionLoginActivity2ToMessagesActivity2(token)
        requireView().findNavController().navigate(action)
    }

    private fun verify() {
        viewModel.init()

        val username = binding.username.text.toString()
        val password = binding.password.text.toString()

        if(username.isBlank() || password.isBlank() || username != password.reversed()){
            if(!checkAlreadyLoggedIn()){
                createError()
            }
            return
        }

        viewModel.logIn(username, password)
    }

    private fun createError() {
        activity?.let {
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