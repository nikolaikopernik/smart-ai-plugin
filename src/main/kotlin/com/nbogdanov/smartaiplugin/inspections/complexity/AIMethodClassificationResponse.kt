package com.nbogdanov.smartaiplugin.inspections.complexity

data class AIMethodClassificationResponse(
    val problems: List<AIClassification>,
    val chatId: String,
)

data class AIClassification(val problematicCode: String,
                            val explanation: String,
                            val score: String) {
}