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
import com.nbogdanov.smartaiplugin.language.LanguageSupport
import com.nbogdanov.smartaiplugin.openai.CONTEXT_WINDOW_LIMIT
import com.nbogdanov.smartaiplugin.openai.OpenAIClientService
import com.nbogdanov.smartaiplugin.statistics.LocalStatistics
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Set of tests to check if we covered all the cases with Metrics/Statistics
 */
class DummyNamesStatisticsTest : LightPlatformTestCase() {
    val virtualPath = mock<VirtualFile>()
    val psiFile = mock<PsiFile>()
    val psiElement = mock<PsiElement>()
    val inspectionManager = mock<InspectionManager>()
    val openAI = FakeOpenAIClientService()
    val shownLocalFixes = mutableListOf<LocalQuickFix>()

    override fun setUp() {
        super.setUp()

        shownLocalFixes.clear()
        whenever(virtualPath.getPath()).thenReturn("/test/path/file.kt")
        whenever(virtualPath.contentsToByteArray()).thenReturn("comecontent".toByteArray())
        whenever(psiFile.getVirtualFile()).thenReturn(virtualPath)
        whenever(psiFile.text).thenReturn(SIMPLE_CLASS)
        whenever(psiFile.findElementAt(any())).thenReturn(psiElement)
        whenever(psiElement.language).thenReturn(JavaLanguage.INSTANCE)
        whenever(inspectionManager.createProblemDescriptor(any<PsiElement>(),
            any<String>(),
            anyBoolean(),
            any(),
            any<ProblemHighlightType>()))
            .thenAnswer { it ->
                shownLocalFixes.add((it.arguments[3] as Array<LocalQuickFix>).first())
                mock<ProblemDescriptor>()
            }

        whenever(psiFile.language).thenReturn(JavaLanguage.INSTANCE)

        ApplicationManager.getApplication().registerServiceInstance(
            OpenAIClientService::class.java,
            openAI)

        val extensionPointName: ExtensionPointName<LanguageSupport> =
            ExtensionPointName("com.nbogdanov.smartaiplugin.languageSupport")

        // Register a mock implementation for the test
        ExtensionTestUtil.maskExtensions(extensionPointName, listOf(FakeLanguageSupport()),
            testRootDisposable)
    }

    /**
     * For DummyNames we can use part of the file so no TooLong issues should be
     * triggered/measured
     */
    fun testNotRecordTooLongContext() {
        whenever(virtualPath.contentsToByteArray()).thenReturn(virtualPath.tooLongFile());

        DummyNamesInspection().checkFile(psiFile, inspectionManager, false)

        shownLocalFixes shouldHaveSize 1
        LocalStatistics.getInstance().state.dataMap.also {
            it shouldHaveSize 2
            it["triggered.dummy_names"] shouldBe 1
            it["fix-shown.dummy_names"] shouldBe 1
        }
    }
}

val SIMPLE_CLASS = """
    class SimpleClass{
      fun a(path: String):String {
        return path.substring(1)
      }
    }
""".trimIndent()

fun VirtualFile.tooLongFile(): ByteArray = (0..CONTEXT_WINDOW_LIMIT * 4).map { 1.toByte() }.toByteArray()