package com.nbogdanov.smartaiplugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.runBlockingCancellable
import com.nbogdanov.smartaiplugin.openai.OpenAI
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.nbogdanov.smartaiplugin.openai.model.AIResponse
import kotlinx.coroutines.CoroutineScope

/**
 * Light-weighted service not reachable anywhere outside the plugin
 */
@Service
final class AIService (private val scope: CoroutineScope): Disposable {
    val openAIClient = OpenAI()

    fun ask(request: AIRequest): AIResponse =
        runBlockingCancellable { openAIClient.ask(request) }

    override fun dispose() {
        openAIClient.close()
    }
}