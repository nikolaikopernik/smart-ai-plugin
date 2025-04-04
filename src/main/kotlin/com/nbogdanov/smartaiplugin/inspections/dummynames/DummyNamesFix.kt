package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameProcessor
import com.nbogdanov.smartaiplugin.statistics.Inspection.dummy_names
import com.nbogdanov.smartaiplugin.statistics.Statistics

/**
 * A simple fix to rename the element according to AI suggestion
 */
class DummyNamesFix(val proposedName: String) : LocalQuickFix {
    override fun getName(): @IntentionName String = "Rename to '$proposedName'"

    override fun getFamilyName(): @IntentionFamilyName String = name

    override fun startInWriteAction(): Boolean = false

    override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
        Statistics.logFixApplied(dummy_names)
        val element: PsiElement = problemDescriptor.psiElement
        if(!ApplicationManager.getApplication().isHeadlessEnvironment) {
            // in tests we don't need to do actual refactor
            RenameProcessor(project,
                element.parent,            // because in problem we highlighted only IDENTIFIER
                proposedName,
                false,
                false).run()
        }
    }
}