package com.nbogdanov.smartaiplugin.openai.model

data class AIMethodClassificationResponse(
    val problems: List<AIClassification>,
    val chatId: String,
)

data class AIClassification(val problematicCode: String,
                            val explanation: String,
                            val score: String) {
}