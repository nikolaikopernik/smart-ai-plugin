package com.nbogdanov.smartaiplugin.language

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.toml.lang.psi.ext.elementType

/**
 * Some language support once needed.
 * For DummyNames we need to locate the problematic code in the file and find exact PSI element.
 */
open class KotlinCodeProcessor : LanguageSupport {

    override fun supportedLanguage(): Language = KotlinLanguage.INSTANCE

    /**
     * As OpenAI can both: return the dummy name only but also a piece of code it contains, we check some
     * aroundings of the given element.
     * Mostly we check all the following siblings (also parent and its siblings)
     * Keep in mind, that this method returns IDENTIFIER, most of the time the actual variable/field/method are
     * parents of those
     */
    override fun findNextNamedIdentifier(element: PsiElement): PsiElement? =
        element.checkSurroundCode { it.isNamed() }


    override fun findNextMethod(element: PsiElement): PsiElement? =
        element.checkSurroundCode { it.elementType == KtTokens.IDENTIFIER && it.parent is KtNamedFunction }

    override fun isMethod(element: PsiElement): Boolean = element is KtNamedFunction

    private fun PsiElement.isNamed(): Boolean =
        this.elementType == KtTokens.IDENTIFIER &&
                when (this.parent) {
                    is KtClass, is KtProperty, is KtNamedFunction, is KtParameter -> true

                    else -> false
                }
}

fun PsiElement.checkSurroundCode(predicate: (PsiElement) -> Boolean): PsiElement? {
    var current = this
    while (!predicate(current) && current.nextSibling != null) {
        current = current.nextSibling
    }
    if (!predicate(current)) {
        //try parents
        current = current.parent
        while (!predicate(current) && current.nextSibling != null) {
            current = current.nextSibling
        }
    }
    return if (predicate(current)) current else null
}