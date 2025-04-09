package com.nbogdanov.smartaiplugin.language

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod

class JavaCodeProcessor : LanguageSupport {

    override fun supportedLanguage(): Language = JavaLanguage.INSTANCE

    override fun findNextNamedIdentifier(element: PsiElement): PsiElement? {
        var found: PsiElement? = null
        element.parent.parent.accept(object : JavaRecursiveElementVisitor() {
            override fun visitIdentifier(identifier: PsiIdentifier) {
                if (found == null) found = identifier
            }
        })
        return found
    }

    override fun findNextMethod(element: PsiElement): PsiElement? {
        var found: PsiElement? = null
        element.parent.parent.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                if (found == null) found = method
            }
        })
        return found
    }

    override fun isMethod(element: PsiElement): Boolean = element is PsiMethod
}