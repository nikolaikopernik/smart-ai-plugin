package com.nbogdanov.smartaiplugin.openai

import com.intellij.lang.Language
import com.nbogdanov.smartaiplugin.openai.model.AIRequest
import com.openai.models.ChatModel
import java.nio.file.Path

class DummyNameAIRequest(val lang: Language, val file: Path) : AIRequest {
    override fun systemMessage() =
        "You are a java developer with 10 ears of experience."

    override fun userMessage() =
        """
            Analyze the provided source code in java and spot all the dummy names 
            in variables fields and methods. Example of dummy names: a,b,d,x,y,a1,v3,foo,bar etc. 
            Be patient and report only severe problems.
        """.intern()

    override fun attachments() = listOf(file)

    override fun modelPreference() = ChatModel.Companion.GPT_4O_MINI


}