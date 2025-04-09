package com.example.aichatbot

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isImage: Boolean = false
)