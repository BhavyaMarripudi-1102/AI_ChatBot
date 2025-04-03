package com.example.aichatbot

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val imageMessage: ImageView = itemView.findViewById(R.id.imageMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        if (message.isImage && message.text.startsWith("data:image")) {
            holder.imageMessage.visibility = View.VISIBLE
            holder.messageText.visibility = View.GONE

            val base64Image = message.text.substringAfter("base64,", "")
            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            holder.imageMessage.setImageBitmap(bitmap)

        } else {
            holder.imageMessage.visibility = View.GONE
            holder.messageText.visibility = View.VISIBLE
            holder.messageText.text = message.text
        }

        holder.messageText.setBackgroundResource(
            if (message.isUser) R.drawable.bg_user_message else R.drawable.bg_ai_message
        )
    }

    override fun getItemCount(): Int = messages.size
}