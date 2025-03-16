package com.nbogdanov.smartaiplugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.runBlockingCancellable
import com.nbogdanov.smartaiplugin.openai.OpenAI
import com.nbogdanov.smartaiplugin.openai.model.AIRequest

/**
 * Light-weighted service not reachable anywhere outside the plugin
 * Here we can plug different LLM providers
 *
 * FIXME switch to coroutines
 */
@Service
class AIService : Disposable {
    private val openAIClient = OpenAI()

    fun <T> ask(request: AIRequest<T>): T? = runBlockingCancellable {
        openAIClient.ask(request)
    }

    override fun dispose() {
        openAIClient.close()
    }
}