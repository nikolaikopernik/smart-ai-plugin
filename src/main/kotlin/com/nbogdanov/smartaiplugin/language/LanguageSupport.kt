package com.nbogdanov.smartaiplugin.language

import com.intellij.lang.Language
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

/**
 * In order to optionally support several languages we need to have several implementation and
 * communicate to them via interfaces (and extension point).
 * IDEA will load only implementations for languages available in users environment
 */
interface LanguageSupport {

    fun supportedLanguage(): Language

    /**
     * For locating the dummy named elements we need this method
     */
    fun findNextNamedIdentifier(element: PsiElement): PsiNamedElement?

    /**
     * We still want to locate code by provided AI code fragment
     * as searching by method name might be complicated.
     * There might be overloaded methods and so on.
     */
    fun findNextMethod(element: PsiElement): PsiElement?
}

/**
 * The extension point which allows to use Java and/or Kotlin analysis
 * depending on if user has necessary plugins
 */
val PLUGIN_EP_NAME: ExtensionPointName<LanguageSupport> =
    ExtensionPointName("com.nbogdanov.smartaiplugin.languageSupport")

fun Language.isSupported() =
    this in PLUGIN_EP_NAME.extensionList
        .map { it.supportedLanguage() }
        .toSet()

/**
 * Seamless using of necessary #LanguageSupport implementation (if loaded)
 * The user don't even need to know about several implementation
 */
fun PsiElement.findNextNamedIdentifier(): PsiNamedElement? =
    PLUGIN_EP_NAME.findFirstSafe { it.supportedLanguage() == this.language }
        ?.findNextNamedIdentifier(this)


fun PsiElement.findNextMethod(): PsiElement? =
    PLUGIN_EP_NAME.findFirstSafe { it.supportedLanguage() == this.language }
        ?.findNextMethod(this)