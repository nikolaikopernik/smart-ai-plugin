package com.nbogdanov.smartaiplugin.openai.model

data class AIResponse(val sourceCode: String,
    val message: String,
    val proposedCode: String)
