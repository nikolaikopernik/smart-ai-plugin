package com.nbogdanov.smartaiplugin.statistics

import com.intellij.openapi.diagnostic.Logger
import com.nbogdanov.smartaiplugin.inspections.complexity.FindComplexMethodsRequest
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.openai.models.ChatCompletion
import com.openai.models.ChatCompletionMessage
import java.io.File
import java.util.Optional
import java.util.UUID

fun Logger.debug(f: () -> String) {
    if (this.isDebugEnabled) this.debug(f.invoke())
}

fun Logger.info(f: () -> String) {
    this.info(f.invoke())
}

fun Logger.warn(f: () -> String) {
    this.warn(f.invoke())
}

fun Logger.warn(ex: Throwable, f: () -> String) {
    if (this.isDebugEnabled) this.warn(f.invoke(), ex)
}

fun Logger.error(ex: Throwable, f: () -> String) {
    this.error(f.invoke(), ex)
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