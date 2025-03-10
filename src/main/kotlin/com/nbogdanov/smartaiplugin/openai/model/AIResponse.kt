package com.nbogdanov.smartaiplugin.openai.model

import com.openai.core.JsonArray
import com.openai.core.JsonObject
import com.openai.core.JsonValue
import com.openai.models.ResponseFormatJsonSchema

/**
 * General answer from AI
 * The idea is that although there will be many different kind of problems,
 * the answer will be very similar always:
 *  - problematic code: to find it in the source code
 *  - explanation: For user to show
 *  - solutionCode: if LLM can suggest the change
 */
data class AIResponse(
    val problems: List<AIProblem>,
    val chatId: String,
)

data class AIProblem(val problematicCode: String,
                     val explanation: String,
                     val solutionCode: String?)

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
