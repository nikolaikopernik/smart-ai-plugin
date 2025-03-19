package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance
import com.nbogdanov.smartaiplugin.AIService
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

class DummyNamesInspectionJavaTest : BasePlatformTestCase() {
    private val service = mock<AIService>()

    override fun setUp() {
        super.setUp()
        ApplicationManager.getApplication().registerServiceInstance(AIService::class.java, service)
        myFixture.enableInspections(DummyNamesInspection())
        myFixture.addFileToProject("String.java", "package java.lang; public final class String {}")
        myFixture.configureByFile("Test.java")
    }

    fun testFixDummyMethodName() {
        doTest(givenProblem = AIProblem("private int get1(int i) {",
            "Some explanation",
            "getCounter"),
            expectedFile = "Test.method.java")
    }

    fun testFixDummyVariable() {
        doTest(givenProblem = AIProblem("var k = get1(1);",
            "Some explanation",
            "counter"),
            expectedFile = "Test.variable.java")
    }

    fun testFixDummyField() {
        doTest(givenProblem = AIProblem("public static final java.lang.String a = \"abc\";",
            "Some explanation",
            "OPERATION_ID"),
            expectedFile = "Test.field.java")
    }

    fun testFixDummyParameter() {
        doTest(givenProblem = AIProblem("int i",
            "Some explanation",
            "increment"),
            expectedFile = "Test.parameter.java")
    }

    fun testCannotFixIfOnlySimpleNameProvided() {
        doTest(givenProblem = AIProblem("i",
            "Some explanation",
            "increment"),
            expectedFile = "Test.java")
    }


    private fun doTest(givenProblem: AIProblem,
                       expectedFile: String) {
        whenever(service.ask(org.mockito.kotlin.any<DummyNamesRequest>())).thenReturn(AIDummyNamesResponse(
            chatId = UUID.randomUUID().toString(),
            problems = listOf(givenProblem))
        )
        // execute
        myFixture.doHighlighting()

        // should be 1 fix
        if(expectedFile == "Test.java") {
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
        return "src/test/testData/java/"
    }
}