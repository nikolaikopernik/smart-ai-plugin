package com.nbogdanov.smartaiplugin.inspections.complexity

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.ui.awt.RelativePoint
import com.nbogdanov.smartaiplugin.AIService
import com.nbogdanov.smartaiplugin.statistics.Inspection
import com.nbogdanov.smartaiplugin.statistics.Statistics
import com.nbogdanov.smartaiplugin.ui.ComplexityRefactorConfirmation
import org.jetbrains.kotlin.idea.codeinsight.utils.findExistingEditor


/**
 * A simple fix to rename the element according to AI suggestion
 */
class ComplexityFix() : LocalQuickFix {
    override fun getName(): @IntentionName String = "DummyAI: Refactor with AI"

    override fun getFamilyName(): @IntentionFamilyName String = name

    override fun startInWriteAction(): Boolean = false

    /**
     * This method is called from EDT so cannot block it and need to do the rest in background
     */
    override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
        Statistics.logFixApplied(Inspection.complexity)
        val element: PsiElement = problemDescriptor.psiElement
        val aiRequest = RefactorMethodRequest(element.language, element.text)
        // Get the element's position in the editor
        val editor = element.findExistingEditor()!!
        val visualPosition: VisualPosition = editor.offsetToVisualPosition(element.textOffset)
        val point = editor.visualPositionToXY(visualPosition)
        val factory = JavaPsiFacade.getElementFactory(project)
        val relativePoint = RelativePoint(editor.contentComponent, point)

        object : Task.Backgroundable(project, "Asking AI", false) {
            override fun run(indicator: ProgressIndicator) {
                val service = ApplicationManager.getApplication().getService(AIService::class.java)
                val response = service.ask(aiRequest)

                // Update UI on EDT
                ApplicationManager.getApplication().invokeLater(Runnable {
                    val confirmation =
                        ComplexityRefactorConfirmation(response!!, project, element.containingFile.language) {
                            applyRefactoring()
                        }
                    JBPopupFactory.getInstance()
                        .createComponentPopupBuilder(confirmation,
                            null
                        )
                        .setCancelOnClickOutside(true)
                        .setResizable(true)
                        .createPopup()
                        .show(relativePoint)
                })
            }
        }.queue()
    }

    private fun applyRefactoring() {
        println("Ready to refactor")
    }
}