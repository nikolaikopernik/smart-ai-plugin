package com.nbogdanov.smartaiplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.nbogdanov.smartaiplugin.misc.AICommunicationCache
import com.nbogdanov.smartaiplugin.openai.OpenAIService
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import kotlinx.coroutines.runBlocking

/**
 * Light-weighted service not reachable anywhere outside the plugin
 * Here we can plug different LLM providers
 */
@Service
class AIService {
    private val cache = AICommunicationCache()

    fun <T> ask(request: AIRequest<T>): T? = runBlocking {
        cache.getOrCall(request) {
            OpenAIService.getInstance().ask(request)
        }
    }


    companion object {
        fun getInstance(): AIService = ApplicationManager.getApplication().getService(AIService::class.java)
    }
}