package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance
import com.nbogdanov.smartaiplugin.AIService
import com.nbogdanov.smartaiplugin.openai.model.AIProblem
import com.nbogdanov.smartaiplugin.openai.model.AIResponse
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

class DummyNamesInspectionTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        var mocked: AIService = mock()
        whenever(mocked.ask(org.mockito.kotlin.any())).thenReturn(AIResponse(
            chatId = UUID.randomUUID().toString(),
            problems = listOf(AIProblem("sink", "desc", "complexitySink"))
        ))
        ApplicationManager.getApplication().registerServiceInstance(AIService::class.java, mocked)
    }

    fun testInspection() {
        myFixture.enableInspections(DummyNamesInspection())
        myFixture.testHighlighting(true, false, false, "test.java")
    }

    override fun getTestDataPath(): String? {
        return "src/test/testData/"
    }
}