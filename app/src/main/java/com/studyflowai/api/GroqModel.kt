package com.studyflowai.api

import com.google.gson.annotations.SerializedName

data class GroqRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<GroqMessage>,
    @SerializedName("temperature") val temperature: Float = 0.7f
)

data class GroqMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class GroqResponse(
    @SerializedName("choices") val choices: List<GroqChoice>
)

data class GroqChoice(
    @SerializedName("message") val message: GroqMessage
)
