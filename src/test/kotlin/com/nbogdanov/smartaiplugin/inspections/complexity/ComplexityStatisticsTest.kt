package com.nbogdanov.smartaiplugin.inspections.complexity

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.ex.QuickFixWrapper
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance
import com.nbogdanov.smartaiplugin.*
import com.nbogdanov.smartaiplugin.openai.OpenAIClientService
import com.nbogdanov.smartaiplugin.statistics.LocalStatistics
import com.openai.core.http.Headers
import com.openai.errors.InternalServerException
import com.openai.errors.OpenAIError
import com.openai.errors.OpenAIIoException
import com.openai.models.ChatCompletion
import io.kotest.matchers.collections.shouldHaveSize

/**
 * Set of tests to check if we covered all the cases with Metrics/Statistics
 * We test on a single file Test.java as the content is not that important
 */
class ComplexityStatisticsTest : BasePlatformTestCase() {
    val openAI = FakeOpenAIClientService()

    override fun setUp() {
        super.setUp()

        myFixture.enableInspections(ComplexityInspection::class.java)
        myFixture.addFileToProject("String.java", "package java.lang; public final class String {}")
        myFixture.configureByFile("Test.java")

        // mocking service so we can provide any OpenAI response
        ApplicationManager.getApplication().registerServiceInstance(
            OpenAIClientService::class.java,
            openAI)
        openAI.respondWith { ChatCompletion.builder().buildSimpleWithBody(body = AI_COMPLEXITY_SIMPLE) }

        //clear the statistics
        LocalStatistics.getInstance().loadState(LocalStatistics.State())
    }

    fun testRecordTooLongContext() {
        myFixture.addFileToProject("TestLong.java", SIMPLE_CLASS.generateLongClass())
        myFixture.configureByFile("TestLong.java")

        myFixture.doHighlighting()

        // somehow all the inspections are enabled again
        // first step of complexity inspection still cau use partial input
        myFixture.getAllQuickFixes().filterComplexityFixes() shouldHaveSize 1
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "fix-shown.complexity" to 1)

        // when we try to get the refactoring of the code for very long method, we get the issue
        myFixture.getAllQuickFixes().filterComplexityFixes().first().invoke(myFixture.project,
            myFixture.editor,
            myFixture.file)
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "fix-shown.complexity" to 1,
            "context-too-large.complexity" to 1)
    }

    fun testNetworkIoIssueHappens() {
        openAI.respondWith { throw OpenAIIoException("Network issue") }

        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 0
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "network-issue.complexity.io_exception" to 1)
    }

    fun testInternalServerErrorHappens() {
        openAI.respondWith {
            throw InternalServerException(500,
                Headers.builder().build(),
                "",
                OpenAIError.builder().build())
        }

        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 0
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "network-issue.complexity.http_5xx" to 1)
    }

    fun testModelDoesNotFinish() {
        openAI.respondWith {
            ChatCompletion.builder().buildSimpleWithBody(
                finishReason = ChatCompletion.Choice.FinishReason.CONTENT_FILTER
            )
        }

        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 0
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "model-not-finished.complexity" to 1)
    }

    fun testCannotParseModelResponse() {
        openAI.respondWith { ChatCompletion.builder().buildSimpleWithBody(body = "SOME WEIRD RESPONSE") }

        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 0
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "model-incorrect-json.complexity" to 1)

    }

    fun testNoProblemsAreSpotted() {
        openAI.respondWith {
            ChatCompletion.builder().buildSimpleWithBody(body = """
            ```json
            [ ]
            ```
        """.trimIndent())
        }

        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 0
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "no-problems-to-show.complexity" to 1)
    }

    fun testCannotLocateProblemCode() {
        openAI.respondWith {
            ChatCompletion.builder().buildSimpleWithBody(body = """
            ```json
            [
                {
                    "problematicCode": "fun unknownFunction()",
                    "explanation": "The functions 'a' is not descriptive of its purpose or the data it holds.",
                    "score": "high"
                }
            ]
            ```
        """.trimIndent())
        }

        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 0
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "cannot-locate-code.complexity.java" to 1)
    }

    fun testNormalCaseTwoIssuesAreShown() {
        openAI.respondWith {
            ChatCompletion.builder().buildSimpleWithBody(body = """
            ```json
                [
                    {
                        "problematicCode": "private int get1(int i)",
                        "explanation": "Method is too complex",
                        "score": "high"
                    },
                    {
                        "problematicCode": "public java.lang.String calculate() {",
                        "explanation": "Method is too complex",
                        "score": "high"
                    }
                ]
            ```
        """.trimIndent())
        }

        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 2
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "fix-shown.complexity" to 2)
    }

    fun testFixIsApplied() {
        // execute
        myFixture.doHighlighting()
        myFixture.getAllQuickFixes() shouldHaveSize 1

        // Now let's refactor method
        openAI.respondWith { ChatCompletion.builder().buildSimpleWithBody(body =
            """
            private int get1(int i) {
               return 1;
            }    
            """.trimIndent())
        }
        myFixture.getAllQuickFixes().first().invoke(myFixture.project, myFixture.editor, myFixture.file)

        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "fix-shown.complexity" to 1,
            "fix-applied.complexity" to 1)
    }

    fun testRefactoringFailedAsModelReturnedNotWhatWeExpect() {
        // execute
        myFixture.doHighlighting()
        myFixture.getAllQuickFixes() shouldHaveSize 1

        // Now let's refactor method
        openAI.respondWith { ChatCompletion.builder().buildSimpleWithBody(body =
            """
            NOT A METHOD AT ALL 
            """.trimIndent())
        }
        myFixture.getAllQuickFixes().first().invoke(myFixture.project, myFixture.editor, myFixture.file)

        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "fix-shown.complexity" to 1,
            "fix-shown-refactor-failed.complexity" to 1)
    }

    fun testRefactoringCancelled() {
        // execute
        myFixture.doHighlighting()
        myFixture.getAllQuickFixes() shouldHaveSize 1

        // Now let's refactor method
        openAI.respondWith { ChatCompletion.builder().buildSimpleWithBody(body =
            """
            cancel
            """.trimIndent())
        }
        myFixture.getAllQuickFixes().first().invoke(myFixture.project, myFixture.editor, myFixture.file)

        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.complexity" to 1,
            "fix-shown.complexity" to 1,
            "fix-shown-refactor-cancelled.complexity" to 1)
    }

    override fun getTestDataPath(): String? {
        return "src/test/testData/java/"
    }
}

fun List<IntentionAction>.filterComplexityFixes(): List<IntentionAction> =
    this.filter { it is ComplexityFix || (QuickFixWrapper.unwrap(it) is ComplexityFix) }