package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.lang.Language
import com.intellij.psi.PsiFile
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.nbogdanov.smartaiplugin.statistics.Inspection

private val mapper: ObjectMapper = jacksonObjectMapper().registerKotlinModule()

class DummyNamesRequest(val lang: Language, val file: PsiFile) : AIRequest<AIDummyNamesResponse> {
    override fun systemMessage() =
        "You are an experienced ${lang.id} developer."

    override fun userMessage() =
        """
            Analyze the provided code in ${lang.id} and spot all the dummy names in variables fields and methods. 
            For every problematic name, include the whole line of code with that name. For explanation use max 2 sentences. 
            And solutionCode should contain the new name only.
            For every spotted problem, use the following format:
              [{"problematicCode": ..., "explanation": ..., "proposedName": ...},{...}]
            Do not add any other text apart of this json.
        """.intern()

    override fun filePath() = file.virtualFile.path

    override fun fileContent() = String(file.virtualFile.contentsToByteArray())

    override fun inspection() = Inspection.dummy_names

    override fun parse(id: String,
                       response: String): AIDummyNamesResponse {
        return AIDummyNamesResponse(
            chatId = id,
            problems = mapper.readValue(response)
        )
    }
}