package com.nbogdanov.smartaiplugin

import com.intellij.codeInspection.GlobalInspectionUtil.createProblem
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import com.nbogdanov.smartaiplugin.openai.DummyNameAIRequest

class DummyNamesAIInspection : LocalInspectionTool() {
    var service: AIService = ApplicationManager.getApplication()
        .getService<AIService>(AIService::class.java)

    override fun processFile(file: PsiFile, manager: InspectionManager): List<ProblemDescriptor?> {
        val response = service.ask(DummyNameAIRequest(file.language, file.virtualFile.toNioPath()))
        return response.problems
            .map { it -> manager.createProblemDescriptor(file,
                "dummy name: ${it.explanation}",
                true,
                emptyArray(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING) }
    }
}