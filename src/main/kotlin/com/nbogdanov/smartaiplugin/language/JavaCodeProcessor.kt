package com.nbogdanov.smartaiplugin.language

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.*

class JavaCodeProcessor : KotlinCodeProcessor() {

    override fun supportedLanguage(): Language = JavaLanguage.INSTANCE

    override fun isNamed(element: PsiElement): Boolean =
        when (element) {
            is PsiClass, is PsiMethod, is PsiLocalVariable, is PsiParameter, is PsiField -> true

            else -> false
        }
}