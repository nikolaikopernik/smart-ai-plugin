package com.nbogdanov.smartaiplugin.openai.model

import com.nbogdanov.smartaiplugin.statistics.Inspection
import com.openai.models.ChatModel

/**
 * General request to AI
 * Different inspections can implement it.
 */
interface AIRequest<T> {
    fun systemMessage(): String
    fun userMessage(): String
    fun filePath(): String
    fun fileContent(): String
    fun modelPreference(): ChatModel
    fun inspection(): Inspection

    fun parse(id: String, response: String): T
}