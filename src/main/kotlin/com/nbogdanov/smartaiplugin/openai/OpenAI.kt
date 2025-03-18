package com.nbogdanov.smartaiplugin.openai


import com.intellij.openapi.diagnostic.Logger
import com.nbogdanov.smartaiplugin.inspections.complexity.FindComplexMethodsRequest
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.nbogdanov.smartaiplugin.statistics.CommunicationIssues
import com.nbogdanov.smartaiplugin.statistics.Statistics
import com.nbogdanov.smartaiplugin.statistics.debug
import com.nbogdanov.smartaiplugin.statistics.warn
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.errors.*
import com.openai.models.*
import kotlinx.coroutines.future.await
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private val log = Logger.getInstance(OpenAI::class.java)

/**
 * Here we apply our OpenAI requests and parse to domain AI responses
 * The main method is suspendable, but it need to be called from within some coroutine
 */
class OpenAI {
    val client = OpenAIOkHttpClient.fromEnv()

    /**
     * Calling OpenAI to get the results
     * If model is not ready/finished or there are some network issues, or the response
     * cannot be parsed - then en null response is returned
     */
    suspend fun <T> ask(query: AIRequest<T>): T? {
        val params = ChatCompletionCreateParams.builder()
            .addSystemMessage(query.systemMessage())
            .addUserMessage(query.userMessage())
            .addUserMessageOfArrayOfContentParts(listOf(
                ChatCompletionContentPart.ofText(
                    ChatCompletionContentPartText.builder()
                        .text(query.fileContent())
                        .build()))
            )
            .model(query.modelPreference())
            .build()
            .also {
                log.debug { "OpenAI request: $it" }
            }

        val chatCompletion = try {
            dummy(query)
//            client.async().chat().completions().create(params)
//                .orTimeout(30, TimeUnit.SECONDS)
//                .await()
//                .also {
//                    // FIXME remove
//                    val response = it.choices().firstOrNull()?.message()?.content()?.orElse(null) ?: "NULL"
//                    File("/opt/workspace/smart-ai-plugin/openai-response.txt").writeText(response)
//                    log.warn {
//                        "OpenAI RESPONSE: $response"
//                    }
//                }
        } catch (ex: Exception) {
            log.warn(ex) { "Cannot get response from OpenAI" }
            val issue = when (ex) {
                is OpenAIIoException, is IOException -> CommunicationIssues.io_exception
                is BadRequestException, is PermissionDeniedException, is NotFoundException, is UnprocessableEntityException, is RateLimitException, is UnauthorizedException -> CommunicationIssues.http_4xx
                is InternalServerException -> CommunicationIssues.http_5xx
                is TimeoutException -> CommunicationIssues.timeout
                else -> CommunicationIssues.other
            }
            Statistics.logNetworkIssue(query.inspection(), issue)
            return null
        }
        return parseResponse(query, chatCompletion)
    }

    fun <T> parseResponse(query: AIRequest<T>, completion: ChatCompletion): T? {
        val choice = completion.choices().firstOrNull()
        if (choice == null || choice.finishReason() != ChatCompletion.Choice.FinishReason.STOP) {
            log.warn { "Empty or incorrect finish reason: ${choice?.finishReason()}" }
            Statistics.logModelNotFinished(query.inspection())
            return null
        }
        if (choice.message().refusal().isPresent) {
            log.warn { "Model refuses to provide response with the reason: ${choice.message().refusal().get()}" }
            Statistics.logModelNotFinished(query.inspection())
            return null
        }
        val text = choice.message().content().orElse("")
            .lines()
            .filter { !it.startsWith("```") }
            .joinToString(separator = "\n")
            .trim()
        try {
            return query.parse(completion.id(), text)
        } catch (ex: Exception) {
            log.warn(ex) { "Cannot parse response from OpenAI" }
            Statistics.logModelJsonIssue(query.inspection())
            return null
        }
    }

    fun <T> dummy(query:AIRequest<T>): ChatCompletion {
        return ChatCompletion.builder()
            .id(UUID.randomUUID().toString())
            .created(1L)
            .model("sdfsdf")
            .choices(listOf(ChatCompletion.Choice.builder()
                .message(ChatCompletionMessage.builder()
                    .content(File( if(query is FindComplexMethodsRequest)
                        "/opt/workspace/smart-ai-plugin/openai-response2.txt"
                    else
                        "/opt/workspace/smart-ai-plugin/openai-response3.txt"
                    ).readText())
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