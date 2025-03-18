package com.nbogdanov.smartaiplugin.inspections.complexity

import com.intellij.lang.Language
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.nbogdanov.smartaiplugin.statistics.Inspection
import com.openai.models.ChatModel


class RefactorMethodRequest(val lang: Language, val method: String) : AIRequest<String> {
    override fun systemMessage() =
        "You are an experienced ${lang.id} developer."

    override fun userMessage() =
        """
            Analyze the provided method code in ${lang.id} and suggest the refactoring to improve readability and minimize the complexity of this method. 
            Make sure that the code you suggest does the same logic, compiles and works.
            As a result, print only the code without any explanations or other text.
        """.intern()

    override fun filePath() = ""

    override fun fileContent() = method

    override fun inspection() = Inspection.complexity

    override fun modelPreference() = ChatModel.Companion.GPT_4O_2024_08_06

    override fun parse(id: String, response: String): String = response
}