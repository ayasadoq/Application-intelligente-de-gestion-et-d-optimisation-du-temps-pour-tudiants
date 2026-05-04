package com.studyflowai.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApi {
    @POST("chat/completions")
    suspend fun generateChatCompletion(
        @Header("Authorization") bearerToken: String,
        @Body request: GroqRequest
    ): GroqResponse
}
