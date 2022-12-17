package com.example.androidmessaging.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.example.androidmessaging.utils.volleySingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


//ViewModel for login activity
class loginViewModel(application: Application) : AndroidViewModel(application) {

    //Initiliasing the viewmodel
    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    //Base url for the REST API request
    private val baseUrl = "https://android-messaging.branch.co/api/"

    //Boolean describing if the user is signed in
    private val _loginAuthorised = MutableLiveData<Boolean>()
    val loginAuthorised : LiveData<Boolean>
        get() = _loginAuthorised

    //String representing the auth_token of the user signed in
    private val _authToken = MutableLiveData<String?>()
    val authToken : LiveData<String?>
        get() = _authToken

    //Boolean representing http request completion
    private val _process = MutableLiveData<Boolean>()
    val process : LiveData<Boolean>
        get() = _process

    //Initialising the variables
    fun init(){
        _loginAuthorised.value = false
        _authToken.value = ""
    }

    //Performing operations when user is logged in
    fun loggedIn(token : String){
        _authToken.value = token
        _loginAuthorised.value = true
        processComplete()
    }

    //Starting the process of http request
    fun processStart(){
        _process.value = false
    }

    //On completion of http request
    fun processComplete(){
        _process.value = true
    }

    //Logging in with the details provided by user
    fun logIn(username : String, pass : String) {

        viewModelScope.launch {
            checkLogin(username, pass)
        }

    }

    //Co-routine function for logging in the user
    private suspend fun checkLogin(username : String, pass : String) {
        return withContext(Dispatchers.IO) {

            val queue = volleySingleton.getInstance(getApplication()).requestQueue

            //Parameters of the user for the login
            val params = JSONObject()
            params.put("username", username)
            params.put("password", pass)

            //Creating the Volley request
            val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST,
                baseUrl + "login",
                params,
                { response ->
                    //On success
//                    println("response -> $response")
//                    println("token -> ${response["auth_token"]}")
                    val token = response["auth_token"]
                    loggedIn(token.toString())
                },
                { error ->
                    //On failure
                    // Handle error
                    println("error -> $error")
                    processComplete()
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): HashMap<String, String> {
                    //Adding headers to the request
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }

            //Adding the request to Volley request queue
            queue.add(jsonObjectRequest)

        }
    }

}