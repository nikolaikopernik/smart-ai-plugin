package com.nbogdanov.smartaiplugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.nbogdanov.smartaiplugin.openai.OpenAI
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.nbogdanov.smartaiplugin.openai.model.AIResponse

/**
 * Light-weighted service not reachable anywhere outside the plugin
 * FIXME switch to coroutines
 */
@Service
class AIService : Disposable {
    private val log: Logger = Logger.getInstance(AIService::class.java)
    private val openAIClient = OpenAI()

    fun ask(request: AIRequest): AIResponse = runBlockingCancellable {
        log.warn("Calling openAI for analyzing file: ${request.attachments().first()}")
        val response = openAIClient.ask(request)
        log.warn("Response:\n" + response.problems);
        return@runBlockingCancellable response;
    }

    override fun dispose() {
        openAIClient.close()
    }
}