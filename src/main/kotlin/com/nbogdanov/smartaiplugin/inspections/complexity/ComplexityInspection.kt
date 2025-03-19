package com.nbogdanov.smartaiplugin.inspections.complexity

import com.intellij.codeInspection.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.nbogdanov.smartaiplugin.AIService
import com.nbogdanov.smartaiplugin.language.findNextMethod
import com.nbogdanov.smartaiplugin.language.isSupported
import com.nbogdanov.smartaiplugin.statistics.Inspection.complexity
import com.nbogdanov.smartaiplugin.statistics.Statistics
import com.nbogdanov.smartaiplugin.statistics.info
import com.nbogdanov.smartaiplugin.statistics.lang
import com.nbogdanov.smartaiplugin.statistics.warn

private val log = Logger.getInstance(ComplexityInspection::class.java)

/**
 * This inspection feeds AI with the whole source file and tries to spot complex methods.
 * For every complex method a quick fix is created. That fix will fire another AI request in order
 * to try to refactor this code.
 */
class ComplexityInspection : LocalInspectionTool() {

    /**
     * As our inspection is per the whole file
     */
    override fun runForWholeFile(): Boolean = true

    /**
     * Actual inspection
     */
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        Statistics.logInspectionStarted(complexity)
        log.info { "Checking AI complex methods in the file ${file.virtualFile.path}" }
        val response = getAIService().ask(FindComplexMethodsRequest(file.language, file))
        if (response == null) {
            return emptyArray()
            // metrics should be updated inside service
        }
        return response.problems
            // only very complex methods deserve refactoring
            .filter { it.score.lowercase() in setOf("high", "very") }
            .also { if (it.isEmpty()) Statistics.logNoProblems(complexity) }
            .map { it ->
                val problematicElement = locateProblem(file, it.problematicCode)
                return@map if (problematicElement == null) {
                    // If we didn't locate the problem based on AI response?
                    // let's not bother the user and ignore it, but need to record this case
                    log.info { "Cannot locate code for '${it.problematicCode}' in file ${file.virtualFile.path}" }
                    Statistics.logCannotLocateProblemCode(complexity, file.language.lang())
                    null
                } else {
                    Statistics.logFixShown(complexity)
                    manager.createProblemDescriptor(problematicElement,
                        "DummyAI: ${it.explanation}",
                        true,
                        arrayOf<LocalQuickFix>(ComplexityFix()),
                        ProblemHighlightType.WARNING)
                }
            }
            .filterNotNull()
            .toTypedArray()
    }

    /**
     * This will remove unnecessary requests to OpenAI for files we are not interested in
     */
    override fun isAvailableForFile(file: PsiFile): Boolean {
        return file.language.isSupported()
    }

    /**
     * This is a simplified implementation.
     * Further improvement would be:
     *  - asking LLM about which line contains the problem
     *  - actually going through all PSI elements near this line to locate the problematic method
     */
    private fun locateProblem(file: PsiFile, problemCodeFragment: String): PsiElement? {
        val offset = file.text.indexOf(problemCodeFragment)
        val element = file.findElementAt(offset)
        if (element == null) {
            log.warn { "Cannot locate problem code in file ${file.virtualFile.path}: $problemCodeFragment" }
            return null
        }
        return element.findNextMethod().also {
            if (it == null) {
                log.warn { "Cannot locate problem code in file ${file.virtualFile.path}: $problemCodeFragment" }
            }
        }
    }

    private fun getAIService() = ApplicationManager.getApplication()
        .getService(AIService::class.java)
}