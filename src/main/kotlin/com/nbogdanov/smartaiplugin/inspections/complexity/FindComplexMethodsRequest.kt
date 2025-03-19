package com.nbogdanov.smartaiplugin.inspections.complexity

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.lang.Language
import com.intellij.psi.PsiFile
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.nbogdanov.smartaiplugin.statistics.Inspection

private val mapper: ObjectMapper = jacksonObjectMapper().registerKotlinModule()


class FindComplexMethodsRequest(val lang: Language, val file: PsiFile) : AIRequest<AIMethodClassificationResponse> {
    override fun systemMessage() =
        "You are an experienced ${lang.id} developer."

    override fun userMessage() =
        """
            Analyze the provided code in ${lang.id} and classify all the method in the file by the cognitive complexity in one of the following scores:
             - trivial - the method implementation is a single line without any complex structures,
             - simple - couple of lines easy to understand without complex structures, 
             - medium - these methods are more complex, but still usually fit in the screen and have a single complex structure,
             - high - even more complex methods with several complex structures, usually those methods are longer and sometimes with bigger nesting,
             - very - the most complex methods where it's hard to follow the code, usually very long with the combination of complex and nested structures
            For the spotted methods return the method signature, a short explanation why you gave it that score and a score itself in the format:
              [{"problematicCode": ..., "explanation": ..., "score":...},{...}]
            Do not add any other text apart of this json.
        """.intern()

    override fun filePath() = file.virtualFile.path

    override fun fileContent() = String(file.virtualFile.contentsToByteArray())

    override fun inspection() = Inspection.complexity

    override fun parse(id: String,
                       response: String): AIMethodClassificationResponse {
        return AIMethodClassificationResponse(
            chatId = id,
            problems = mapper.readValue(response)
        )
    }
}