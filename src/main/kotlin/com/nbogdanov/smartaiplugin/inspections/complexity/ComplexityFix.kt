package com.nbogdanov.smartaiplugin.inspections.complexity

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.awt.RelativePoint
import com.nbogdanov.smartaiplugin.AIService
import com.nbogdanov.smartaiplugin.language.findTopLevelMethods
import com.nbogdanov.smartaiplugin.statistics.Inspection
import com.nbogdanov.smartaiplugin.statistics.Statistics
import com.nbogdanov.smartaiplugin.statistics.warn
import com.nbogdanov.smartaiplugin.ui.ComplexityRefactorConfirmationComponent
import org.jetbrains.kotlin.idea.base.util.reformat
import org.jetbrains.kotlin.idea.codeinsight.utils.findExistingEditor

private val log = Logger.getInstance(ComplexityFix::class.java)

/**
 * This is not that simple fix.
 * It first shows the popup with the proposal from LLM on how to improve the method.
 * If user agrees with it, it will replace the old method with the one (or several) proposed methods.
 */
class ComplexityFix() : LocalQuickFix {
    override fun getName(): @IntentionName String = "DummyAI: Refactor with AI"

    override fun getFamilyName(): @IntentionFamilyName String = name

    override fun startInWriteAction(): Boolean = false

    /**
     * This method is called from EDT so cannot block it and need to do the rest in background
     */
    override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
        val element: PsiElement = problemDescriptor.psiElement
        val aiRequest = RefactorMethodRequest(element.language, element.text)
        // Get the element's position in the editor
        val editor = element.findExistingEditor()!!
        val visualPosition: VisualPosition = editor.offsetToVisualPosition(element.textOffset)
        val point = editor.visualPositionToXY(visualPosition)
        val relativePoint = RelativePoint(editor.contentComponent, point)

        object : Task.Backgroundable(project, "Asking AI...", false) {
            override fun run(indicator: ProgressIndicator) {
                val service = ApplicationManager.getApplication().getService(AIService::class.java)
                val response = service.ask(aiRequest)

                // Update UI on EDT
                ApplicationManager.getApplication().invokeLater(Runnable {
                    ComplexityRefactorConfirmationComponent(
                        code = response!!,
                        project = project,
                        language = element.containingFile.language,
                        relativePoint = relativePoint,
                        apply = { applyRefactoring(element, project, response) },
                        cancel = { Statistics.logFixShownRefactorCancelled() }
                    ).showConfirmation()
                })
            }
        }.queue()
    }

    private fun applyRefactoring(element: PsiElement, project: Project, newCode: String) {
        val psiFile: PsiFile = element.containingFile
        val newPsiFile = PsiFileFactory.getInstance(project).createFileFromText(psiFile.language, newCode)
        val parent = element.parent
        val methods = newPsiFile.findTopLevelMethods()
        if (methods.isEmpty()) {
            // not good, we cannot find the necessary code, should not proceed with refactoring
            log.warn { "Cannot interpret the suggestion from AI. Aborting refactoring." }
            Statistics.logFixShownRefactorFailed()
        } else {
            WriteCommandAction.runWriteCommandAction(project, Runnable {
                methods.forEach {
                    parent.addBefore(it, element)
                }
                element.delete()
                parent.reformat(false)
                Statistics.logFixApplied(Inspection.complexity)
            })
        }
    }
}