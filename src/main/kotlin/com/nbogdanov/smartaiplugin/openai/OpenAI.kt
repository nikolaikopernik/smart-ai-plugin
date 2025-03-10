package com.nbogdanov.smartaiplugin.openai


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.nbogdanov.smartaiplugin.openai.model.AIResponse
import com.nbogdanov.smartaiplugin.openai.model.DummyNameAIRequest
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.ChatCompletion
import com.openai.models.ChatCompletionContentPart
import com.openai.models.ChatCompletionContentPartText
import com.openai.models.ChatCompletionCreateParams
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import java.nio.file.Path


class OpenAI {
    val mapper: ObjectMapper = jacksonObjectMapper().registerKotlinModule()

    /**
     * Calling OpenAI to get the results
     */
    suspend fun ask(query: AIRequest): AIResponse {
        val client = OpenAIOkHttpClient.fromEnv()

        val params = ChatCompletionCreateParams.builder()
            .addSystemMessage(query.systemMessage())
            .addUserMessage(userMessageFormatted(query.userMessage()))
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
        return parseResponse(chatCompletion)
    }

    fun parseResponse(completion: ChatCompletion): AIResponse {
        val choice = completion.choices().firstOrNull()
        if (choice == null || choice.finishReason() != ChatCompletion.Choice.FinishReason.STOP) {
            throw RuntimeException("Empty or incorrect finish reason: ${choice?.finishReason()}")
        }
        if (choice.message().refusal().isPresent) {
            throw RuntimeException("Model refuses to provide response with the reason: ${
                choice.message().refusal().get()
            }")
        }
        val text = choice.message().content().orElse("")
            .lines()
            .filter { !it.startsWith("```") }
            .joinToString(separator = " ")
            .trim()
        return AIResponse(problems = mapper.readValue(text),
            chatId = completion.id())
    }

    fun userMessageFormatted(message: String) =
        message + "\n" + """
            For every spotted problem, use the following format:
              [{problematicCode:..., explanation:...,solutionCode:...},{...}]
            Do not add any other text apart of this json.  
        """.trimIndent()

}

fun main() {
    runBlocking {
        val q = DummyNameAIRequest(Path.of("/opt/workspace/smart-ai-plugin/src/test/resources/test.java"))
        val result = OpenAI().ask(q)
        println(result)
    }
    println("Done")
}