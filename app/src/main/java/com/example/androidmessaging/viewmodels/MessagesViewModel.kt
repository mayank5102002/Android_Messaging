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

//Viewmodel for messages activity and conversation activity
class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    //Initialising the viewmodel
    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    //Base url for the REST API request
    private val baseUrl = "https://android-messaging.branch.co/api/"

    //auth_token of the current user
    private val _authToken = MutableLiveData<String?>()
    val authToken : LiveData<String?>
        get() = _authToken

    //Message threads provided by the server
    private val _threads = MutableLiveData<MutableList<MessageThread>>()
    val threads : LiveData<MutableList<MessageThread>>
        get() = _threads

    //Message threads sorted with respect to the customers
    private val _sortedCustomerData = MutableLiveData<HashMap<String, ArrayList<MessageThread>>>()
    val sortedCustomerData : LiveData<HashMap<String, ArrayList<MessageThread>>>
        get() = _sortedCustomerData

    //Latest messages of every customer to be shown in form of threads
    private val _activeThreads = MutableLiveData<MutableList<MessageThread>>()
    val activeThreads : LiveData<MutableList<MessageThread>>
        get() = _activeThreads

    //Boolean determining that the message has been sent
    private val _messageSent = MutableLiveData<Boolean>()
    val messageSent : LiveData<Boolean>
        get() = _messageSent

    //Initialising the token value of the current user
    fun setToken(token : String) {
        _authToken.value = token
    }

    //Setting the messages to the provided list of messages
    private fun setMessages(data : ArrayList<MessageThread>){
        _threads.value = data
    }

    //Sorting the threads on basis of customer and getting the active threads
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

    //Getting the message thread for a specific customer provided
    fun getDataForCustomer(customerId : String) : ArrayList<MessageThread> {
        return _sortedCustomerData.value?.get(customerId)!!
    }

    //Performing operations on successfully sending the message
    fun messageSentComplete(message : MessageThread, id : String){
        _sortedCustomerData.value?.get(id)?.add(message)
        _messageSent.value = true
    }

    //Function to get all the messages from the server
    fun getMessages(){
        viewModelScope.launch {
            messages()
        }
    }

    //Sending the message with the provided body and the thread id
    fun sendMessage(body : String, threadId : Int){
         viewModelScope.launch {
             send(body, threadId)
         }
    }

    //Function to reset the messages
    fun reset(){
        viewModelScope.launch {
            resetCall()
        }
    }

    //Co-routine function to get all the messages
    private suspend fun messages(){
        return withContext(Dispatchers.IO){

            val queue = volleySingleton.getInstance(getApplication()).requestQueue

            //Creating volley request
            val jsonArrayRequest = object : JsonArrayRequest(
                Method.GET,
                baseUrl + "messages",
                null,
                { response ->
                    //On success
                    val data = ArrayList<MessageThread>()
                    for(i in 0 until response.length()){
                        val temp = getObject(response, i)
                        data.add(temp)
                    }
                    //Sorting the messages on basis of their timestamp
                    data.sortBy { it.timestamp }
                    setMessages(data)
                    println("Call successfull")
                },
                { error ->
                    //On failure
                    // Handle error
                    println("error -> $error")
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): HashMap<String, String> {
                    //Adding headers to the request
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["X-Branch-Auth-Token"] = "${_authToken.value}"
                    return headers
                }
            }

            //Adding request to the volley request queue
            queue.add(jsonArrayRequest)

        }
    }

    //Co-routine fuction to send the messsage with the provided body and thread id by the user
    private suspend fun send(body : String, threadId : Int){
        return withContext(Dispatchers.IO){

            val queue = volleySingleton.getInstance(getApplication()).requestQueue

            //Parameters for the message to be sent
            val params = JSONObject()
            params.put("thread_id", threadId)
            params.put("body", body)

            //Creating request
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                baseUrl + "messages",
                params,
                { response ->
                    //On success
                    println("response -> $response")
                    val jsonObject = response

                    //Getting the message fields
                    val messageId = jsonObject["id"] as Int
                    val customerId = jsonObject["user_id"] as String
                    val agentId = jsonObject["agent_id"] as String
                    val timestamp = jsonObject["timestamp"] as String

                    //Creatin the message object
                    val temp = MessageThread(messageId,
                        threadId,
                        customerId,
                        agentId,
                        body,
                        timestamp
                    )

                    println(temp)
                    //Indicating the current message is sent successfully
                    messageSentComplete(temp, customerId)
                },
                { error ->
                    //On failure
                    // Handle error
                    println("error -> $error")
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): HashMap<String, String> {
                    //Adding headers to the request
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["X-Branch-Auth-Token"] = "${_authToken.value}"
                    return headers
                }
            }

            //Adding request to the volley request queue
            queue.add(jsonObjectRequest)
        }
    }

    //Function to send the reset request for the current user
    private suspend fun resetCall() {
        return withContext(Dispatchers.IO){
            val queue = volleySingleton.getInstance(getApplication()).requestQueue

            //Creating request
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                baseUrl + "reset",
                null,
                { response ->
                    //On success
                    println("response -> $response")
                },
                { error ->
                    //On failure
                    // Handle error
                    println("error -> $error")
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): HashMap<String, String> {
                    //Adding headers to the request
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["X-Branch-Auth-Token"] = "${_authToken.value}"
                    return headers
                }
            }

            //Adding the queue to the volley request queue
            queue.add(jsonObjectRequest)
        }
    }

    //Function to get the ith index message from the provided JSONArray
    fun getObject(response : JSONArray, i : Int) : MessageThread {
        val jsonObject = response.getJSONObject(i)
//        println(jsonObject)

        //Getting the message fields
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

        //Creating the message object
        val temp = MessageThread(messageId,
            threadId,
            customerId,
            agentId,
            body,
            timestamp
        )

        //Returning the message object
        return temp
    }

}