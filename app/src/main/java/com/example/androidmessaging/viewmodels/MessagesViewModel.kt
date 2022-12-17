package com.example.androidmessaging.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.androidmessaging.MessageThread
import com.example.androidmessaging.utils.volleySingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val baseUrl = "https://android-messaging.branch.co/api/"

    private val _authToken = MutableLiveData<String?>()
    val authToken : LiveData<String?>
        get() = _authToken

    private val _threads = MutableLiveData<MutableList<MessageThread>>()
    val threads : LiveData<MutableList<MessageThread>>
        get() = _threads

    private val _sortedCustomerData = MutableLiveData<HashMap<String, ArrayList<MessageThread>>>()
    val sortedCustomerData : LiveData<HashMap<String, ArrayList<MessageThread>>>
        get() = _sortedCustomerData

    private val _activeThreads = MutableLiveData<MutableList<MessageThread>>()
    val activeThreads : LiveData<MutableList<MessageThread>>
        get() = _activeThreads

    private val _messageSent = MutableLiveData<Boolean>()
    val messageSent : LiveData<Boolean>
        get() = _messageSent

    fun setToken(token : String) {
        _authToken.value = token
    }

    private fun setMessages(data : ArrayList<MessageThread>){
        _threads.value = data
    }

    fun getActiveThreads() : MutableList<MessageThread>? {

        //Temporary map to store data
        val temp = HashMap<String, ArrayList<MessageThread>>()
        //Dividing all the threads customer based
        for(text in _threads.value!!){
            if(temp[text.customerId].isNullOrEmpty()){
                temp[text.customerId] = ArrayList()
            }
            temp[text.customerId]?.add(text)
        }

        _sortedCustomerData.value = temp

        //Temporary list to store data
        val temp2 = ArrayList<MessageThread>()

        //Getting the last messages of all the active threads
        for(texts in _sortedCustomerData.value!!) {
//            println("${texts.key} + ${texts.value.last()}")
            temp2.add(texts.value.last())
        }

        _activeThreads.value = temp2

        return _activeThreads.value
    }

    fun getDataForCustomer(customerId : String) : ArrayList<MessageThread> {
        return _sortedCustomerData.value?.get(customerId)!!
    }

    fun messageSentComplete(message : MessageThread, id : String){
        _sortedCustomerData.value?.get(id)?.add(message)
        _messageSent.value = true
    }

    fun getMessages(){
        viewModelScope.launch {
            messages()
        }
    }

    fun sendMessage(body : String, threadId : Int){
         viewModelScope.launch {
             send(body, threadId)
         }
    }

    fun reset(){
        viewModelScope.launch {
            resetCall()
        }
    }

    private suspend fun messages(){
        return withContext(Dispatchers.IO){

            val queue = volleySingleton.getInstance(getApplication()).requestQueue

            val jsonArrayRequest = object : JsonArrayRequest(
                Method.GET,
                baseUrl + "messages",
                null,
                { response ->
                    val data = ArrayList<MessageThread>()
                    for(i in 0 until response.length()){
                        val temp = getObject(response, i)
                        data.add(temp)
                    }
                    data.sortBy { it.timestamp }
                    setMessages(data)
                    println("Call successfull")
                },
                { error ->
                    // Handle error
                    println("error -> $error")
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): HashMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["X-Branch-Auth-Token"] = "${_authToken.value}"
                    return headers
                }
            }

            queue.add(jsonArrayRequest)

        }
    }

    private suspend fun send(body : String, threadId : Int){
        return withContext(Dispatchers.IO){

            val queue = volleySingleton.getInstance(getApplication()).requestQueue

            val params = JSONObject()
            params.put("thread_id", threadId)
            params.put("body", body)

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                baseUrl + "messages",
                params,
                { response ->
                    println("response -> $response")
                    val jsonObject = response

                    val messageId = jsonObject["id"] as Int
                    val customerId = jsonObject["user_id"] as String
                    val agentId = jsonObject["agent_id"] as String
                    val timestamp = jsonObject["timestamp"] as String

                    val temp = MessageThread(messageId,
                        threadId,
                        customerId,
                        agentId,
                        body,
                        timestamp
                    )

                    println(temp)
                    messageSentComplete(temp, customerId)
                },
                { error ->
                    // Handle error
                    println("error -> $error")
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): HashMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["X-Branch-Auth-Token"] = "${_authToken.value}"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)
        }
    }

    private suspend fun resetCall() {
        return withContext(Dispatchers.IO){
            val queue = volleySingleton.getInstance(getApplication()).requestQueue

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                baseUrl + "reset",
                null,
                { response ->
                    println("response -> $response")
                },
                { error ->
                    // Handle error
                    println("error -> $error")
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): HashMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["X-Branch-Auth-Token"] = "${_authToken.value}"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)
        }
    }

    fun getObject(response : JSONArray, i : Int) : MessageThread {
        val jsonObject = response.getJSONObject(i)
//        println(jsonObject)

        val messageId = jsonObject["id"] as Int
        val threadId = jsonObject["thread_id"] as Int
        val customerId = jsonObject["user_id"] as String
        val agentId : String = if(jsonObject["agent_id"].toString() != "null"){
            jsonObject["agent_id"] as String
        } else {
            "-1"
        }

        val body = jsonObject["body"] as String
        val timestamp = jsonObject["timestamp"] as String

        val temp = MessageThread(messageId,
            threadId,
            customerId,
            agentId,
            body,
            timestamp
        )

        return temp
    }

}