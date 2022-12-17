package com.example.androidmessaging

//Data class for message thread indicating all its fields
data class MessageThread(
    val messageId : Int,
    val threadId : Int,
    val customerId : String,
    val agentId : String,
    val body : String,
    val timestamp : String
)