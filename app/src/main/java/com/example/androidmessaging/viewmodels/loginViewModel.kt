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


class loginViewModel(application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val baseUrl = "https://android-messaging.branch.co/api/"

    private val _loginAuthorised = MutableLiveData<Boolean>()
    val loginAuthorised : LiveData<Boolean>
        get() = _loginAuthorised

    private val _authToken = MutableLiveData<String?>()
    val authToken : LiveData<String?>
        get() = _authToken

    private val _process = MutableLiveData<Boolean>()
    val process : LiveData<Boolean>
        get() = _process

    fun init(){
        _loginAuthorised.value = false
        _authToken.value = ""
    }

    fun loggedIn(token : String){
        _authToken.value = token
        _loginAuthorised.value = true
        processComplete()
    }

    fun processStart(){
        _process.value = false
    }

    fun processComplete(){
        _process.value = true
    }

    fun logIn(username : String, pass : String) {

        viewModelScope.launch {
            checkLogin(username, pass)
        }

    }

    private suspend fun checkLogin(username : String, pass : String) {
        return withContext(Dispatchers.IO) {

            val queue = volleySingleton.getInstance(getApplication()).requestQueue

            val params = JSONObject()
            params.put("username", username)
            params.put("password", pass)

            val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST,
                baseUrl + "login",
                params,
                { response ->
//                    println("response -> $response")
//                    println("token -> ${response["auth_token"]}")
                    val token = response["auth_token"]
                    loggedIn(token.toString())
                },
                { error ->
                    // Handle error
                    println("error -> $error")
                    processComplete()
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): HashMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)

        }
    }

}