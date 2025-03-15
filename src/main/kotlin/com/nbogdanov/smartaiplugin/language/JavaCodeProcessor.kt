package com.nbogdanov.smartaiplugin.language

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

class JavaCodeProcessor : LanguageSupport {

    override fun supportedLanguage(): Language = JavaLanguage.INSTANCE

    override fun findNextNamedIdentifier(element: PsiElement): PsiElement? {
        var current = element
        while (current.elementType != JavaTokenType.IDENTIFIER && current.nextSibling != null) {
            current = current.nextSibling
        }
        return if (current.elementType == JavaTokenType.IDENTIFIER) current else null
    }
}