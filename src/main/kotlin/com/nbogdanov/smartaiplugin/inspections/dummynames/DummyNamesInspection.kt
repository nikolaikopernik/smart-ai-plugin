package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.codeInspection.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.nbogdanov.smartaiplugin.AIService
import com.nbogdanov.smartaiplugin.language.findNextNamedIdentifier
import com.nbogdanov.smartaiplugin.language.isSupported

class DummyNamesInspection : LocalInspectionTool() {

    /**
     * Actual inspection
     */
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        val response = getAIService().ask(DummyNamesRequest(file.language, file.virtualFile.toNioPath()))
        return response.problems
            .map { it ->
                val problematicElement = locateProblem(file, it.problematicCode)
                return@map if (problematicElement == null)
                // If we didn't locate the problem based on AI response?
                // let's not bother the user and ignore it, but need to record this case
                    null
                else manager.createProblemDescriptor(problematicElement,
                    "DummyAI: ${it.explanation}. Proposed name: ${it.solutionCode}",
                    true,
                    arrayOf<LocalQuickFix>(DummyNamesFix(it.solutionCode!!)),
                    ProblemHighlightType.WARNING)
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
        val element = file.findElementAt(offset) ?: return null
        return element.findNextNamedIdentifier()?.let { it ->
            // final check
            // if AI returned name only - let's double check we found the correct element
            if (problemCodeFragment.indexOf(" ") < 0 && it.name.equals(problemCodeFragment))
                it
            else
                null
        }
    }

    private fun getAIService() = ApplicationManager.getApplication()
        .getService(AIService::class.java)
}