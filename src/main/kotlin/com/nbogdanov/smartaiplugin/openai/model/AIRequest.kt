package com.nbogdanov.smartaiplugin.openai.model

import com.nbogdanov.smartaiplugin.statistics.Inspection
import com.openai.models.ChatModel

/**
 * General request to AI
 * Different inspections can implement it.
 * Also there is a method to parse the response. So different inspections won't have any limitations
 * to use their own response schema
 */
interface AIRequest<T> {
    fun systemMessage(): String
    fun userMessage(): String
    fun filePath(): String
    fun fileContent(): String
    fun modelPreference(): ChatModel = ChatModel.Companion.GPT_4O_2024_08_06
    fun inspection(): Inspection
    fun canUsePartialFile(): Boolean = true
    fun parse(id: String, response: String): T
}