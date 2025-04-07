package com.nbogdanov.smartaiplugin

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.testFramework.registerServiceInstance
import com.nbogdanov.smartaiplugin.inspections.dummynames.DummyNamesRequest
import com.nbogdanov.smartaiplugin.misc.AICommunicationCache.Companion.setClockTestOnly
import com.nbogdanov.smartaiplugin.openai.OpenAIClientService
import com.openai.models.ChatCompletion
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import java.time.Clock.fixed
import java.time.Instant
import java.time.ZoneId
import java.util.*


class AIRequestCachingTest : LightPlatformTestCase() {
    val openAI = spy(FakeOpenAIClientService())
    val psiFile = mock<PsiFile>()
    var counter: Int = 0

    override fun setUp() {
        super.setUp()

        counter = 0
        // mocking service so we can provide any OpenAI response
        ApplicationManager.getApplication().registerServiceInstance(
            OpenAIClientService::class.java,
            openAI)
        openAI.respondWith {
            ChatCompletion.builder().buildSimpleWithBody(body = """
            ```json
            [
                {
                    "problematicCode": "var k = get1(1);",
                    "explanation": "${counter++}",
                    "proposedName": "counter"
                }
            ]
            ```
        """.trimIndent())
        }

        val virtualFile = mock<VirtualFile>()
        whenever(virtualFile.path).thenReturn("/path/to/file/${UUID.randomUUID()}.java")
        whenever(psiFile.virtualFile).thenReturn(virtualFile)
        whenever(psiFile.text).thenAnswer { it -> UUID.randomUUID().toString() }
    }

    fun testShouldCacheAIRequests() {
        val r1 = AIService.getInstance().ask(DummyNamesRequest(JavaLanguage.INSTANCE, psiFile))
        val r2 = AIService.getInstance().ask(DummyNamesRequest(JavaLanguage.INSTANCE, psiFile))
        val r3 = AIService.getInstance().ask(DummyNamesRequest(JavaLanguage.INSTANCE, psiFile))

        r1!!.problems.first().explanation shouldBeEqual "0"
        r2!!.problems.first().explanation shouldBeEqual "0"
        r3!!.problems.first().explanation shouldBeEqual "0"
    }

    fun testShouldDoACallEvery5Second() {
        val now = Instant.now()

        runBlocking {
            setClockTestOnly(fixed(now, ZoneId.systemDefault()))
            val r1 = AIService.getInstance().ask(DummyNamesRequest(JavaLanguage.INSTANCE, psiFile))
            setClockTestOnly(fixed(now.plusSeconds(6), ZoneId.systemDefault()))
            val r2 = AIService.getInstance().ask(DummyNamesRequest(JavaLanguage.INSTANCE, psiFile))

            r1!!.problems.first().explanation shouldBeEqual "0"
            r2!!.problems.first().explanation shouldBeEqual "1"
        }
    }

    fun testShouldKeepCacheIfFileNotChanged() {
        // file content is the same
        whenever(psiFile.text).thenReturn("some content")
        val now = Instant.now()
        runBlocking {
            setClockTestOnly(fixed(now, ZoneId.systemDefault()))
            val r1 = AIService.getInstance().ask(DummyNamesRequest(JavaLanguage.INSTANCE, psiFile))
            setClockTestOnly(fixed(now.plusSeconds(6), ZoneId.systemDefault()))
            val r2 = AIService.getInstance().ask(DummyNamesRequest(JavaLanguage.INSTANCE, psiFile))

            r1!!.problems.first().explanation shouldBeEqual "0"
            r2!!.problems.first().explanation shouldBeEqual "0"
        }
    }
}