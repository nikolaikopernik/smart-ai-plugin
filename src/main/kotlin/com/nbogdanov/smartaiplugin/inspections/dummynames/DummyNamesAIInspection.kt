package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.nbogdanov.smartaiplugin.AIService

class DummyNamesAIInspection : LocalInspectionTool() {

    /**
     * Actual inspection
     */
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
        println("Running dummy inspection for ${file.virtualFile.toNioPath()}")
        val response = getService().ask(DummyNameAIRequest(file.language, file.virtualFile.toNioPath()))
        return response.problems
            .map { it ->
                manager.createProblemDescriptor(locateProblem(file, it.problematicCode),
                    "DummyAI: ${it.explanation}",
                    true,
                    emptyArray(),
                    ProblemHighlightType.WARNING)
            }
            .toTypedArray()
    }

    private fun locateProblem(file: PsiFile, codeFragment: String): PsiElement {
        val offset = file.text.indexOf(codeFragment)
        return PsiTreeUtil.findCommonParent(
            file.findElementAt(offset),
            file.findElementAt(offset + codeFragment.length)
        ) ?: file
    }

    private fun getService() = ApplicationManager.getApplication()
        .getService(AIService::class.java)
}