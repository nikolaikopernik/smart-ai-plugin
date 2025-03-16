package com.nbogdanov.smartaiplugin.openai


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.openapi.diagnostic.Logger
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.nbogdanov.smartaiplugin.openai.model.AIResponse
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.*
import kotlinx.coroutines.future.await
import java.util.Optional
import java.util.UUID

private val log = Logger.getInstance(OpenAI::class.java)

/**
 * Here we apply our domain AI requests and parse to domain AI responses
 */
class OpenAI {

    val mapper: ObjectMapper = jacksonObjectMapper().registerKotlinModule()
    val client = OpenAIOkHttpClient.fromEnv()

    /**
     * Calling OpenAI to get the results
     */
    suspend fun ask(query: AIRequest): AIResponse {
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
//        val chatCompletion = dummy()
        val chatCompletion = client.async()
            .chat().completions()
            .create(params)
            .await()
            .also { log.warn("AI RESPONSE:\n" + it.choices().first().message().content().orElse("EMPTY")) }
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
            .also { log.info("OpenAI response:\n $it") }
            .lines()
            .filter { !it.startsWith("```") }
            .joinToString(separator = " ")
            .trim()
        return AIResponse(problems = mapper.readValue(text),
            chatId = completion.id())
    }

    fun dummy(): ChatCompletion {
        return ChatCompletion.builder()
            .id(UUID.randomUUID().toString())
            .created(1L)
            .model("sdfsdf")
            .choices(listOf(ChatCompletion.Choice.builder()
                .message(ChatCompletionMessage.builder()
                    .content(this.javaClass.getResource("/dummy.txt").readText())
                    .refusal(Optional.empty<String>())
                    .build())
                .index(1L)
                .logprobs(Optional.empty())
                .finishReason(ChatCompletion.Choice.FinishReason.STOP)
                .build()))
            .build()
    }

    fun close() {
        client.close()
    }
}