package com.nbogdanov.smartaiplugin.inspections.dummynames

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.PsiElement
import com.nbogdanov.smartaiplugin.language.LanguageSupport

class FakeLanguageSupport : LanguageSupport {
    override fun supportedLanguage(): Language = JavaLanguage.INSTANCE

    override fun findNextNamedIdentifier(element: PsiElement): PsiElement? = element

    override fun findNextMethod(element: PsiElement): PsiElement? = element

    override fun isMethod(element: PsiElement): Boolean = true
}