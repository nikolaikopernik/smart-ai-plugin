package com.nbogdanov.smartaiplugin.inspections.complexity

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.lang.Language
import com.intellij.psi.PsiFile
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.nbogdanov.smartaiplugin.openai.model.AIResponse
import com.nbogdanov.smartaiplugin.statistics.Inspection
import com.openai.models.ChatModel

private val mapper: ObjectMapper = jacksonObjectMapper().registerKotlinModule()


class FindComplexMethodsRequest(val lang: Language, val file: PsiFile) : AIRequest<AIResponse> {
    override fun systemMessage() =
        "You are an experienced ${lang.id} developer."

    override fun userMessage() =
        """
            Analyze the provided code in ${lang.id} and spot all functions or methods wich are 
            very complex to analyze. It might be very long methods or methods with a lot of conditional 
            expressions with different purposes. 
            For the spotted methods return the method signature and a short explanation why you think the complexity of this method is very high.
            If the code you see is a normal or medium complexity code then return an empty list.
              [{"problematicCode": ..., "explanation": ...},{...}]
            Do not add any other text apart of this json.
        """.intern()

    override fun filePath() = file.virtualFile.path

    override fun fileContent() = String(file.virtualFile.contentsToByteArray())

    override fun inspection() = Inspection.complexity

    override fun modelPreference() = ChatModel.Companion.GPT_4O_2024_08_06

    override fun parse(id: String,
                       response: String): AIResponse {
        return AIResponse(
            chatId = id,
            problems = mapper.readValue(response)
        )
    }
}