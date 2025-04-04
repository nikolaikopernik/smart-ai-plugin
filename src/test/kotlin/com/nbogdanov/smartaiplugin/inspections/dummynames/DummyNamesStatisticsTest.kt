package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.ex.QuickFixWrapper
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance
import com.nbogdanov.smartaiplugin.FakeOpenAIClientService
import com.nbogdanov.smartaiplugin.buildSimpleWithBody
import com.nbogdanov.smartaiplugin.openai.CONTEXT_WINDOW_LIMIT
import com.nbogdanov.smartaiplugin.openai.OpenAIClientService
import com.nbogdanov.smartaiplugin.shouldHaveOnlyCounters
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
class DummyNamesStatisticsTest : BasePlatformTestCase() {
    val openAI = FakeOpenAIClientService()

    override fun setUp() {
        super.setUp()

        myFixture.enableInspections(DummyNamesInspection::class.java)
        myFixture.addFileToProject("String.java", "package java.lang; public final class String {}")
        myFixture.configureByFile("Test.java")

        // mocking services and extension point
        ApplicationManager.getApplication().registerServiceInstance(
            OpenAIClientService::class.java,
            openAI)

        //clear the statistics
        LocalStatistics.getInstance().loadState(LocalStatistics.State())
    }

    /**
     * For DummyNames we can use part of the file so no TooLong issues should be
     * triggered/measured
     */
    fun testNotRecordTooLongContext() {
        myFixture.addFileToProject("TestLong.java", SIMPLE_CLASS.generateLongClass())
        myFixture.configureByFile("TestLong.java")

        myFixture.doHighlighting()

        //somehow all the inspections are enabled again
        myFixture.getAllQuickFixes().filterDummyFixes() shouldHaveSize 1
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.dummy_names" to 1,
            "fix-shown.dummy_names" to 1)
    }

    fun testNetworkIoIssueHappens() {
        openAI.respondWith { throw OpenAIIoException("Network issue") }

        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 0
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.dummy_names" to 1,
            "network-issue.dummy_names.io_exception" to 1)
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
            "triggered.dummy_names" to 1,
            "network-issue.dummy_names.http_5xx" to 1)
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
            "triggered.dummy_names" to 1,
            "model-not-finished.dummy_names" to 1)
    }

    fun testCannotParseModelResponse() {
        openAI.respondWith { ChatCompletion.builder().buildSimpleWithBody(body = "SOME WEIRD RESPONSE") }

        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 0
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.dummy_names" to 1,
            "model-incorrect-json.dummy_names" to 1)

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
            "triggered.dummy_names" to 1,
            "no-problems-to-show.dummy_names" to 1)
    }

    fun testCannotLocateProblemCode() {
        openAI.respondWith {
            ChatCompletion.builder().buildSimpleWithBody(body = """
            ```json
            [
                {
                    "problematicCode": "fun unknownFunction()",
                    "explanation": "The functions 'a' is not descriptive of its purpose or the data it holds.",
                    "proposedName": "element"
                }
            ]
            ```
        """.trimIndent())
        }

        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 0
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.dummy_names" to 1,
            "cannot-locate-code.dummy_names.java" to 1)
    }

    fun testNormalCaseTwoIssuesAreShown() {
        openAI.respondWith {
            ChatCompletion.builder().buildSimpleWithBody(body = """
            ```json
                [
                    {
                        "problematicCode": "var k = get1(1);",
                        "explanation": "Some explanation",
                        "proposedName": "counter"
                    },
                    {
                        "problematicCode": "public static final java.lang.String a = \"abc\"",
                        "explanation": "The functions 'a' is not descriptive of its purpose or the data it holds.",
                        "proposedName": "OPERATION_ID"
                    }
                ]
            ```
        """.trimIndent())
        }

        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 2
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.dummy_names" to 1,
            "fix-shown.dummy_names" to 2)
    }

    fun testFixIsApplied() {
        // execute
        myFixture.doHighlighting()

        myFixture.getAllQuickFixes() shouldHaveSize 1
        myFixture.getAllQuickFixes().first().invoke(myFixture.project, myFixture.editor, myFixture.file)

        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.dummy_names" to 1,
            "fix-shown.dummy_names" to 1,
            "fix-applied.dummy_names" to 1)
    }

    override fun getTestDataPath(): String? {
        return "src/test/testData/java/"
    }
}

val SIMPLE_CLASS = """
    public class Test {
    public static final java.lang.String a = "abc";

    private int get1(int i) {
        return 1;
    }

    public java.lang.String calculate() {
        var k = get1(1);
        return "Sdf" + a;
    }
}
""".trimIndent()

fun String.generateLongClass() = this.replace("abc",
    String((0..CONTEXT_WINDOW_LIMIT * 4).map { 1.toByte() }.toByteArray()))

fun List<IntentionAction>.filterDummyFixes(): List<IntentionAction> =
    this.filter { it is DummyNamesFix || (QuickFixWrapper.unwrap(it) is DummyNamesFix ) }