package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameProcessor

/**
 * Simple fix to rename the element according to AI suggestion
 */
class DummyNamesFix(val proposedName: String) : LocalQuickFix {
    override fun getName(): @IntentionName String = "Rename to '$proposedName'"

    override fun getFamilyName(): @IntentionFamilyName String = name

    override fun startInWriteAction(): Boolean = false

    override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
        val element: PsiElement = problemDescriptor.psiElement
        RenameProcessor(project,
            element,
            proposedName,
            false,
            false).run()
    }
}