package com.nbogdanov.smartaiplugin.openai

import com.intellij.openapi.application.ApplicationManager
import com.openai.models.ChatCompletion
import com.openai.models.ChatModel

/**
 * Actual client wrapped in a service so that it's easier to test the whole
 * plugin in isolation
 */
interface OpenAIClientService {
    suspend fun callOpenAI(systemMessage: String,
                           userMessage: String,
                           context: String,
                           model: ChatModel): ChatCompletion

    companion object {
        fun getInstance(): OpenAIClientService =
            ApplicationManager.getApplication().getService(OpenAIClientService::class.java)
    }
}