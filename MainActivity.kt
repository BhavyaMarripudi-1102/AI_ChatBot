package com.example.aichatbot

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var micButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.getDefault()
            }
        }

        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        micButton = findViewById(R.id.btnMic)
        recyclerView = findViewById(R.id.chatRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        chatViewModel.messages.observe(this) { messages ->
            chatAdapter = ChatAdapter(messages)
            recyclerView.adapter = chatAdapter
            recyclerView.scrollToPosition(messages.size - 1)

            val latest = messages.lastOrNull()
            if (latest != null && !latest.isUser) {
                textToSpeech.speak(latest.text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }

        sendButton.setOnClickListener {
            val text = messageInput.text.toString()
            if (text.isNotEmpty()) {
                chatViewModel.sendMessage(text)
                messageInput.setText("")
            }
        }

        micButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            result?.firstOrNull()?.let {
                messageInput.setText(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.shutdown()
        speechRecognizer.destroy()
    }
}