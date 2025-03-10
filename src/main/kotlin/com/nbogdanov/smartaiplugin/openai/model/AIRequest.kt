package com.nbogdanov.smartaiplugin.openai.model

import com.openai.models.ChatModel
import java.nio.file.Path

/**
 * General request to AI
 * Different inspections can implement it.
 */
interface AIRequest {
    fun systemMessage(): String
    fun userMessage(): String
    fun attachments(): List<Path>
    fun modelPreference(): ChatModel
}