package com.example.aichatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ChatViewModel : ViewModel() {
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val openRouterKey = "Bearer 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" // Replace with your OpenRouter key
    private val stabilityApiKey = "Bearer sk-AkTbNlqFJ74nVy4jeWMOWG3Du4AMstk7MyJSEa8YgChG94WF" // Replace with your Stability AI key

    fun sendMessage(userMessage: String) {
        _messages.postValue(_messages.value?.plus(ChatMessage(userMessage, true)))

        if (userMessage.contains("draw", true) || userMessage.contains("picture", true) || userMessage.contains("image", true)) {
            generateImage(userMessage)
        } else {
            generateTextResponse(userMessage)
        }
    }

    private fun generateTextResponse(prompt: String) {
        val jsonBody = JSONObject().apply {
            put("model", "openai/gpt-3.5-turbo")
            put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            put("temperature", 0.7)
        }

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .addHeader("Authorization", openRouterKey)
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://AIChatbot.com") // required
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                postError("❌ Failed to connect for text")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    postError("⚠️ Text response error")
                    return
                }

                try {
                    val content = JSONObject(body)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    _messages.postValue(_messages.value?.plus(ChatMessage(content, false)))
                } catch (e: Exception) {
                    postError("⚠️ Error parsing text response")
                }
            }
        })
    }

    private fun generateImage(prompt: String) {
        val client = OkHttpClient()

        val body = JSONObject().apply {
            put("prompt", prompt)
            put("model", "stable-diffusion-xl-1024-v1-0")  // New model ID
            put("output_format", "base64_json")
        }

        val request = Request.Builder()
            .url("https://api.stability.ai/v2beta/stable-image/generate/core")
            .post(body.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .addHeader("Authorization", "Bearer YOUR_STABILITY_API_KEY")  // 🔑 Replace this!
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                postError("Image generation failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body?.string()
                if (!response.isSuccessful || responseStr == null) {
                    postError("Image API error: ${response.code}")
                    return
                }

                try {
                    val json = JSONObject(responseStr)
                    val base64 = json.getJSONArray("images").getString(0)
                    val imageData = "data:image/png;base64,$base64"
                    _messages.postValue(_messages.value?.plus(ChatMessage(imageData, false, true)))
                } catch (e: Exception) {
                    postError("Error parsing image: ${e.message}")
                }
            }
        })
    }

    private fun postError(msg: String) {
        _messages.postValue(_messages.value?.plus(ChatMessage(msg, false)))
    }
}