package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.testFramework.registerServiceInstance
import com.nbogdanov.smartaiplugin.*
import com.nbogdanov.smartaiplugin.language.LanguageSupport
import com.nbogdanov.smartaiplugin.openai.OpenAIClientService
import com.nbogdanov.smartaiplugin.statistics.LocalStatistics
import com.openai.core.http.Headers
import com.openai.errors.InternalServerException
import com.openai.errors.OpenAIError
import com.openai.errors.OpenAIIoException
import com.openai.models.ChatCompletion
import io.kotest.matchers.collections.shouldHaveSize
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Set of tests to check if we covered all the cases with Metrics/Statistics
 */
class DummyNamesStatisticsTest : LightPlatformTestCase() {
    val vrFileForAnalysis = mock<VirtualFile>()
    val psiFile = mock<PsiFile>()
    val psiElement = mock<PsiElement>()
    val inspectionManager = mock<InspectionManager>()
    val openAI = FakeOpenAIClientService()
    val shownLocalFixes = mutableListOf<LocalQuickFix>()

    override fun setUp() {
        super.setUp()

        // mocking the psi stuff
        whenever(vrFileForAnalysis.getPath()).thenReturn("/test/path/file.java")
        whenever(psiFile.getVirtualFile()).thenReturn(vrFileForAnalysis)
        whenever(psiFile.text).thenReturn(SIMPLE_CLASS)
        whenever(psiFile.findElementAt(any())).thenReturn(psiElement)
        whenever(psiElement.language).thenReturn(JavaLanguage.INSTANCE)
        whenever(psiElement.parent).thenReturn(psiElement)
        whenever(psiFile.language).thenReturn(JavaLanguage.INSTANCE)
        whenever(inspectionManager.createProblemDescriptor(any<PsiElement>(),
            any<String>(),
            anyBoolean(),
            any(),
            any<ProblemHighlightType>()))
            .thenAnswer { it ->
                shownLocalFixes.add((it.arguments[3] as Array<LocalQuickFix>).first())
                mock<ProblemDescriptor>()
            }

        // mocking services and extension point
        ApplicationManager.getApplication().registerServiceInstance(
            OpenAIClientService::class.java,
            openAI)
        val extensionPointName: ExtensionPointName<LanguageSupport> =
            ExtensionPointName("com.nbogdanov.smartaiplugin.languageSupport")

        // Register a mock implementation for the test
        ExtensionTestUtil.maskExtensions(extensionPointName, listOf(FakeLanguageSupport()),
            testRootDisposable)

        //clear the statistics
        shownLocalFixes.clear()
        LocalStatistics.getInstance().loadState(LocalStatistics.State())
    }

    /**
     * For DummyNames we can use part of the file so no TooLong issues should be
     * triggered/measured
     */
    fun testNotRecordTooLongContext() {
        whenever(psiFile.text).thenReturn(psiFile.tooLongContent());

        DummyNamesInspection().checkFile(psiFile, inspectionManager, false)

        shownLocalFixes shouldHaveSize 1
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.dummy_names" to 1,
            "fix-shown.dummy_names" to 1)
    }

    fun testNetworkIoIssueHappens() {
        openAI.respondWith { throw OpenAIIoException("Network issue") }

        DummyNamesInspection().checkFile(psiFile, inspectionManager, false)

        shownLocalFixes shouldHaveSize 0
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

        DummyNamesInspection().checkFile(psiFile, inspectionManager, false)

        shownLocalFixes shouldHaveSize 0
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

        DummyNamesInspection().checkFile(psiFile, inspectionManager, false)

        shownLocalFixes shouldHaveSize 0
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.dummy_names" to 1,
            "model-not-finished.dummy_names" to 1)
    }

    fun testCannotParseModelResponse() {
        openAI.respondWith { ChatCompletion.builder().buildSimpleWithBody(body = "SOME WEIRD RESPONSE") }

        DummyNamesInspection().checkFile(psiFile, inspectionManager, false)

        shownLocalFixes shouldHaveSize 0
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

        DummyNamesInspection().checkFile(psiFile, inspectionManager, false)

        shownLocalFixes shouldHaveSize 0
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

        DummyNamesInspection().checkFile(psiFile, inspectionManager, false)

        shownLocalFixes shouldHaveSize 0
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
                        "problematicCode": "fun a(path: String):String {",
                        "explanation": "The functions 'a' is not descriptive of its purpose or the data it holds.",
                        "proposedName": "element"
                    },
                    {
                        "problematicCode": "fun a(path: String):String {",
                        "explanation": "The functions 'a' is not descriptive of its purpose or the data it holds.",
                        "proposedName": "element"
                    }
                ]
            ```
        """.trimIndent())
        }

        DummyNamesInspection().checkFile(psiFile, inspectionManager, false)

        shownLocalFixes shouldHaveSize 2
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.dummy_names" to 1,
            "fix-shown.dummy_names" to 2)
    }

    fun testFixIsApplied() {
        DummyNamesInspection().checkFile(psiFile, inspectionManager, false)

        shownLocalFixes shouldHaveSize 1

        val problem = mock<ProblemDescriptor>()
        whenever(problem.psiElement).thenReturn(psiElement)


        shownLocalFixes.first().applyFix(project, problem)
        LocalStatistics.getInstance().shouldHaveOnlyCounters(
            "triggered.dummy_names" to 1,
            "fix-shown.dummy_names" to 1,
            "fix-applied.dummy_names" to 1)
    }
}

val SIMPLE_CLASS = """
    class SimpleClass{
      fun a(path: String):String {
        return path.substring(1)
      }
    }
""".trimIndent()