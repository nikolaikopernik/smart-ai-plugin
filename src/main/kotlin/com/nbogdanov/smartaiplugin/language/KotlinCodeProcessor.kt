package com.nbogdanov.smartaiplugin.language

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens

class KotlinCodeProcessor : LanguageSupport {
    override fun supportedLanguage(): Language = KotlinLanguage.INSTANCE

    override fun findNextNamedIdentifier(element: PsiElement): PsiElement? {
        var current = element
        while (current.elementType != KtTokens.IDENTIFIER && current.nextSibling != null) {
            current = current.nextSibling
        }
        return if (current.elementType == KtTokens.IDENTIFIER) current else null
    }
}