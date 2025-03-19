package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.codeInspection.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.nbogdanov.smartaiplugin.AIService
import com.nbogdanov.smartaiplugin.language.findNextNamedIdentifier
import com.nbogdanov.smartaiplugin.language.isSupported
import com.nbogdanov.smartaiplugin.statistics.Inspection.dummy_names
import com.nbogdanov.smartaiplugin.statistics.Statistics
import com.nbogdanov.smartaiplugin.statistics.lang
import com.nbogdanov.smartaiplugin.statistics.warn

private val log = Logger.getInstance(DummyNamesInspection::class.java)

/**
 * This inspection feeds AI with the whole source file and tries to spot dummy names for methods, params, variable etc.
 * For every problematic name it tries to suggest the proper name based on context
 */
class DummyNamesInspection : LocalInspectionTool() {

    /**
     * As our inspection is per the whole file
     */
    override fun runForWholeFile(): Boolean = true

    /**
     * Actual inspection
     */
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        Statistics.logInspectionStarted(dummy_names)
        val response = getAIService().ask(DummyNamesRequest(file.language, file))
        if (response == null) {
            // metrics should be updated inside service
            return emptyArray()
        }
        return response.problems
            .also { if (it.isEmpty()) Statistics.logNoProblems(dummy_names) }
            .map { it ->
                val problematicElement = locateProblem(file, it.problematicCode)
                return@map if (problematicElement == null) {
                    // If we didn't locate the problem based on AI response?
                    // let's not bother the user and ignore it, but need to record this case
                    Statistics.logCannotLocateProblemCode(dummy_names, file.language.lang())
                    null
                } else {
                    Statistics.logFixShown(dummy_names)
                    manager.createProblemDescriptor(problematicElement,
                        "DummyAI: Rename <code>${it.problematicCode}</code> to <code>${it.solutionCode}</code>. ${it.explanation}",
                        true,
                        arrayOf<LocalQuickFix>(DummyNamesFix(it.solutionCode!!)),
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

    private fun locateProblem(file: PsiFile, problemCodeFragment: String): PsiElement? {
        val offset = file.text.indexOf(problemCodeFragment)
        val element = file.findElementAt(offset)
        if (element == null) {
            log.warn { "Cannot locate problem code in file ${file.virtualFile.path}: $problemCodeFragment" }
            return null
        }
        return element.findNextNamedIdentifier()?.let { it ->
            // final check
            // if AI returned name only - let's double-check we found the correct element
            if (problemCodeFragment.indexOf(" ") > 0 || it.text.equals(problemCodeFragment))
                it
            else
                null
        }.also {
            if (it == null) {
                log.warn { "Cannot locate problem code in file ${file.virtualFile.path}: $problemCodeFragment" }
            }
        }
    }

    private fun getAIService() = ApplicationManager.getApplication()
        .getService(AIService::class.java)
}