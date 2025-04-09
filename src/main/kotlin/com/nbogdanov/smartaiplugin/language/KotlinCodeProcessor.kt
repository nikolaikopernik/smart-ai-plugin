package com.nbogdanov.smartaiplugin.language

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
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
    override fun findNextNamedIdentifier(element: PsiElement): PsiElement? {
        var found: PsiElement? = null
        element.parent.parent.accept(object : KtTreeVisitorVoid() {
            override fun visitClassOrObject(classOrObject: KtClassOrObject) {
                super.visitClassOrObject(classOrObject)
                found = classOrObject.nameIdentifier
            }

            override fun visitProperty(property: KtProperty) {
                super.visitProperty(property)
                found = property.nameIdentifier
            }

            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)
                found = function.nameIdentifier
            }

            override fun visitParameter(parameter: KtParameter) {
                super.visitParameter(parameter)
                found = parameter.nameIdentifier
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