package com.nbogdanov.smartaiplugin.language

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.*
import org.toml.lang.psi.ext.elementType

class JavaCodeProcessor : LanguageSupport {

    override fun supportedLanguage(): Language = JavaLanguage.INSTANCE

    override fun findNextNamedIdentifier(element: PsiElement): PsiNamedElement? =
        element.checkSurroundCode { it.isNamed() }?.parent as PsiNamedElement

    override fun findNextMethod(element: PsiElement): PsiElement? =
        element.checkSurroundCode {
            it.elementType == JavaTokenType.IDENTIFIER && it.parent is PsiMethod
        }?.parent

    override fun isMethod(element: PsiElement): Boolean = element is PsiMethod

    private fun PsiElement.isNamed(): Boolean =
        this.elementType == JavaTokenType.IDENTIFIER &&
                when (this.parent) {
                    is PsiClass, is PsiMethod, is PsiLocalVariable, is PsiParameter, is PsiField -> true

                    else -> false
                }
}