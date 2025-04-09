package com.nbogdanov.smartaiplugin.language

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

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
    override fun findNextNamedIdentifier(element: PsiElement, name: String): PsiElement? {
        var found: PsiElement? = null
        element.parent.parent.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
                super.visitNamedDeclaration(declaration)
                if(declaration.nameIdentifier?.text == name) found = declaration.nameIdentifier
            }
        })
        return found
    }


    override fun findNextMethod(element: PsiElement): PsiElement? {
        var found: PsiElement? = null
        element.parent.parent.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)
                found = function
            }
        })
        return found
    }

    override fun isMethod(element: PsiElement): Boolean = element is KtNamedFunction

}