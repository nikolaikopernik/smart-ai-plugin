package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.openai.core.JsonArray
import com.openai.core.JsonObject
import com.openai.core.JsonValue
import com.openai.models.ResponseFormatJsonSchema

/**
 * Answer to have for Dummy names inspection
 */
data class AIDummyNamesResponse(
    val problems: List<AIProblem>,
    val chatId: String,
)

data class AIProblem(val problematicCode: String,
                     val explanation: String,
                     val proposedName: String?)

/**
 * OpenAI has a way to provide a structured response,
 * However not all the models support it (including my test GTP4.0_mini)
 * Plus it always complains about more things.
 * Can be checked later. For now, we rely on JSON instruction right in the conversation.
 */
val RESPONSE_SCHEMA = ResponseFormatJsonSchema.JsonSchema.builder()
    .name("problemresponse")
    .putAdditionalProperty("type", JsonValue.from("array"))
    .putAdditionalProperty("items", JsonArray.of(listOf(JsonObject.of(mapOf(
        "type" to JsonValue.from("object"),
        "properties" to JsonObject.of(mapOf("problematicCode" to JsonObject.of(mapOf("type" to JsonValue.from("string"))),
            "explanation" to JsonObject.of(mapOf("type" to JsonValue.from("string"))),
            "solutionCode" to JsonObject.of(mapOf("type" to JsonValue.from("string"))))))))))
    .build();
