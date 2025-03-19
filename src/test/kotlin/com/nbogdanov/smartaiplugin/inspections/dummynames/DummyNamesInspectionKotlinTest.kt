package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance
import com.nbogdanov.smartaiplugin.AIService
import com.nbogdanov.smartaiplugin.openai.model.AIProblem
import com.nbogdanov.smartaiplugin.openai.model.AIGeneralResponse
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

class DummyNamesInspectionKotlinTest : BasePlatformTestCase() {
    private val service = mock<AIService>()

    override fun setUp() {
        super.setUp()
        ApplicationManager.getApplication().registerServiceInstance(AIService::class.java, service)

        myFixture.enableInspections(DummyNamesInspection())
        myFixture.addFileToProject("String.kt", "package java.lang; public final class String {}")
        myFixture.configureByFile("Test.kt")
    }

    fun testFixDummyMethodName() {
        doTest(givenProblem = AIProblem("private fun get1(i: Int): Int {",
            "Some explanation",
            "getCounter"),
            expectedFile = "Test.method.kt")
    }

    fun testFixDummyVariable() {
        doTest(givenProblem = AIProblem("var k = get1(1)",
            "Some explanation",
            "incrementValue"),
            expectedFile = "Test.variable.kt")
    }

    fun testFixDummyField() {
        doTest(givenProblem = AIProblem("private val a = \"abc\"",
            "Some explanation",
            "CONSTANT"),
            expectedFile = "Test.field.kt")
    }

    fun testFixDummyParameter() {
        doTest(givenProblem = AIProblem("i: Int",
            "Some explanation",
            "idIncremental"),
            expectedFile = "Test.parameter.kt")
    }

    fun testCannotFixIfOnlySimpleNameProvided() {
        doTest(givenProblem = AIProblem("i",
            "Some explanation",
            "increment"),
            expectedFile = "Test.kt")
    }


    private fun doTest(givenProblem: AIProblem,
                       expectedFile: String) {
        whenever(service.ask(any<DummyNamesRequest>())).thenReturn(AIGeneralResponse(
            chatId = UUID.randomUUID().toString(),
            problems = listOf(givenProblem))
        )
        // execute
        myFixture.doHighlighting()

        // should be 1 fix
        if(expectedFile == "Test.kt") {
            assertEquals(0, myFixture.getAllQuickFixes().size)
            myFixture.checkResultByFile(expectedFile)
        } else {
            val fixes = myFixture.getAllQuickFixes()
            assertEquals(1, fixes.size)
            fixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
            myFixture.checkResultByFile(expectedFile)
        }
    }

    override fun getTestDataPath(): String? {
        return "src/test/testData/kotlin/"
    }
}