package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.nbogdanov.smartaiplugin.openai.OpenAIClientService
import com.openai.models.ChatCompletion
import com.openai.models.ChatCompletionMessage
import com.openai.models.ChatModel
import java.util.*

class FakeOpenAIClientService : OpenAIClientService {
    private var action: (() -> ChatCompletion)? = null

    override suspend fun callOpenAI(systemMessage: String,
                                    userMessage: String,
                                    context: String,
                                    model: ChatModel): ChatCompletion {
        return action?.invoke()
            ?: ChatCompletion.builder()
                .choices(listOf(ChatCompletion.Choice.builder()
                    .message(ChatCompletionMessage.builder().content("""
                        ```json
                        [
                            {
                                "problematicCode": "fun a(path: String):String {",
                                "explanation": "The functions 'a' is not descriptive of its purpose or the data it holds.",
                                "proposedName": "element"
                            }
                        ]
                        ```
                    """.trimIndent())
                        .refusal(Optional.empty())
                        .build())
                    .finishReason(ChatCompletion.Choice.FinishReason.STOP)
                    .index(101L)
                    .logprobs(Optional.empty())
                    .build()
                ))
                .id(UUID.randomUUID().toString())
                .created(101L)
                .model("model")
                .build()

    }

    fun respondWith(action: () -> ChatCompletion) {
        this.action = action
    }


}