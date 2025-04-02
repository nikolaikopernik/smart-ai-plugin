package com.nbogdanov.smartaiplugin.openai

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.nbogdanov.smartaiplugin.statistics.debug
import com.nbogdanov.smartaiplugin.statistics.info
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.credential.BearerTokenCredential
import com.openai.models.*
import kotlinx.coroutines.future.await
import java.util.concurrent.TimeUnit

private val log = Logger.getInstance(OpenAIClientServiceImpl::class.java)

class OpenAIClientServiceImpl : OpenAIClientService, Disposable {
    val client: OpenAIClient

    init {
        val token = System.getenv("OPENAI_API_KEY") ?: System.getProperty("openaiApiToken")
        log.info { "Initializing OpenAI client with token $token" }
        client = OpenAIOkHttpClient.builder()
            .credential(BearerTokenCredential.create(token))
            .build()
    }


    override suspend fun callOpenAI(systemMessage: String,
                                    userMessage: String,
                                    context: String,
                                    model: ChatModel): ChatCompletion {
        val params = ChatCompletionCreateParams.builder()
            .addSystemMessage(systemMessage)
            .addUserMessage(userMessage)
            .addUserMessageOfArrayOfContentParts(listOf(
                ChatCompletionContentPart.ofText(
                    ChatCompletionContentPartText.builder()
                        .text(context)
                        .build()))
            )
            .model(model)
            .build()
            .also {
                log.debug { "OpenAI request: $it" }
            }

        return client.async().chat().completions().create(params)
            .orTimeout(30, TimeUnit.SECONDS)
            .await()
            .also {
                log.info { "OpenAI response: $it" }
            }
    }

    override fun dispose() {
        client.close()
    }
}