package com.example.androidmessaging

data class MessageThread(
    val messageId : Int,
    val threadId : Int,
    val customerId : String,
    val agentId : String,
    val body : String,
    val timestamp : String
)