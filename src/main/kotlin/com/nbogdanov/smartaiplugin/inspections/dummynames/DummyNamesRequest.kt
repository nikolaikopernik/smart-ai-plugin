package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.lang.Language
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.openai.models.ChatModel
import java.nio.file.Path

class DummyNamesRequest(val lang: Language, val file: Path) : AIRequest {
    override fun systemMessage() =
        "You are a java developer with 10 years of experience."

    override fun userMessage() =
        """
            Analyze the provided code in ${lang.id} and spot all the dummy names in variables fields and methods. 
            Do not suggest further code improvements apart from naming. Do not suggest improvements for names which are at least 4 characters long.
            For every spotted problem, use the following format:
              [{"problematicCode": <piece_of_problematic_code>, "explanation": <explanation_in_max_2_sentences>, "solutionCode": <proposed_solution> },{...}]
            Do not add any other text apart of this json.
        """.intern()

    override fun attachments() = listOf(file)

    override fun modelPreference() = ChatModel.Companion.GPT_4O_MINI


}