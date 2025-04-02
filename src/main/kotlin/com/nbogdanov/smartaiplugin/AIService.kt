package com.nbogdanov.smartaiplugin

import com.intellij.openapi.components.Service
import com.nbogdanov.smartaiplugin.openai.OpenAIService
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import kotlinx.coroutines.runBlocking

/**
 * Light-weighted service not reachable anywhere outside the plugin
 * Here we can plug different LLM providers
 */
@Service
class AIService {

    fun <T> ask(request: AIRequest<T>): T? = runBlocking {
        OpenAIService.getInstance().ask(request)
    }
}