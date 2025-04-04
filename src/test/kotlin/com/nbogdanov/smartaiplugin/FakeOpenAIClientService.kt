package com.nbogdanov.smartaiplugin

import com.nbogdanov.smartaiplugin.openai.OpenAIClientService
import com.openai.models.ChatCompletion
import com.openai.models.ChatCompletionMessage
import com.openai.models.ChatModel
import java.util.*

/**
 * Simple to use fake AI service so we can manipulate it in the tests
 */
class FakeOpenAIClientService : OpenAIClientService {
    private var action: (() -> ChatCompletion)? = null

    override suspend fun callOpenAI(systemMessage: String,
                                    userMessage: String,
                                    context: String,
                                    model: ChatModel): ChatCompletion {
        return action?.invoke()
            ?: ChatCompletion.builder().buildSimpleWithBody()
    }

    fun respondWith(action: () -> ChatCompletion) {
        this.action = action
    }
}


fun ChatCompletion.Builder.buildSimpleWithBody(body: String = AI_DUMMY_NAMES_SIMPLE,
                                               finishReason: ChatCompletion.Choice.FinishReason = ChatCompletion.Choice.FinishReason.STOP): ChatCompletion =
    this
        .choices(listOf(ChatCompletion.Choice.builder()
            .message(ChatCompletionMessage.Companion.builder().content(body)
                .refusal(Optional.empty())
                .build())
            .finishReason(finishReason)
            .index(101L)
            .logprobs(Optional.empty())
            .build()
        ))
        .id(UUID.randomUUID().toString())
        .created(101L)
        .model("model")
        .build()

val AI_DUMMY_NAMES_SIMPLE = """
                        ```json
                        [
                            {
                                "problematicCode": "fun a(path: String):String {",
                                "explanation": "The functions 'a' is not descriptive of its purpose or the data it holds.",
                                "proposedName": "element"
                            }
                        ]
                        ```
                    """.trimIndent()