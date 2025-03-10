package com.nbogdanov.smartaiplugin.openai


import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.nbogdanov.smartaiplugin.openai.model.DummyNameAIRequest
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.ChatCompletionContentPart
import com.openai.models.ChatCompletionContentPartText
import com.openai.models.ChatCompletionCreateParams
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import java.nio.file.Path


class OpenAI {


    /**
     * Calling OpenAI to get the results
     */
    suspend fun ask(query: AIRequest): String {
        val client = OpenAIOkHttpClient.fromEnv()

        val params = ChatCompletionCreateParams.builder()
            .addSystemMessage(query.systemMessage())
            .addUserMessage(query.userMessage())
            .addUserMessageOfArrayOfContentParts(query.attachments()
                .map {
                    ChatCompletionContentPart.ofText(
                        ChatCompletionContentPartText.builder()
                            .text(it.toFile().readText())
                            .build()
                    )
                })
            .model(query.modelPreference())
            .build()
        val chatCompletion = client.async()
            .chat().completions()
            .create(params)
            .await()
        client.close()
        return chatCompletion.choices()
            .joinToString(separator = " ") { it.message().content().orElse("") }
    }
}

fun main() {
    runBlocking {
        val q = DummyNameAIRequest(Path.of("/opt/workspace/smart-ai-plugin/src/test/resources/test.java"))
        val result = OpenAI().ask(q)
        println(result)
    }
    println("Done")
}