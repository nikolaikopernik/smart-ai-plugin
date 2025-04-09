package com.nbogdanov.smartaiplugin.inspections.dummynames

/**
 * Answer to have for Dummy names inspection
 */
data class AIDummyNamesResponse(
    val problems: List<AIProblem>,
    val chatId: String,
)

data class AIProblem(val problematicCode: String,
                     val problematicName: String,
                     val explanation: String,
                     val proposedName: String?)
