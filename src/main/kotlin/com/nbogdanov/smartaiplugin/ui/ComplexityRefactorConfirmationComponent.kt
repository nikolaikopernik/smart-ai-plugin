package com.nbogdanov.smartaiplugin.ui

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.EditorTextField
import com.intellij.ui.LanguageTextField
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent

/**
 * This is a simple ugly confirmation for AI refactoring suggestion.
 * Main requirements and features:
 *  - it should show a suggested code fragment
 *  - would be nice to have language highlighting support
 *  - it also provides a Apply button to confirm the refactoring
 */
class ComplexityRefactorConfirmationComponent(val code: String,
                                              val project: Project,
                                              val language: Language,
                                              val relativePoint: RelativePoint,
                                              val apply: () -> Unit,
                                              val cancel: () -> Unit) : JComponent() {
    private var popup: JBPopup? = null
    private val title = JBLabel("Proposed refactoring:")

    init {
        this.border = BorderFactory.createEmptyBorder(4, 4, 4, 4)

        // LanguageTestField provides some lang support (based on language)
        // but that's not a full highlighting like in editor
        // TODO play with it later
        val editorTextField: EditorTextField = LanguageTextField(language, project, code)
        editorTextField.setOneLineMode(false)
        editorTextField.addSettingsProvider({ editor ->
            editor.isViewer = false
        })

        val button = JButton("Apply")
        button.addActionListener({ e ->
            this.popup?.dispose()
            apply()
        })

        this.layout = BorderLayout()
        this.add(title, BorderLayout.NORTH)
        this.add(JBScrollPane(editorTextField), BorderLayout.CENTER)
        this.add(JBPanel<Nothing>(FlowLayout(FlowLayout.LEFT)).also { it.add(button) }, BorderLayout.SOUTH)
        this.preferredSize = Dimension(400, 400)
    }

    /**
     * Use this method to show component
     */
    fun showConfirmation() {
        this.popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(this, null)
            .setCancelOnClickOutside(true)
            .setResizable(true)
            .createPopup();
        this.popup!!.show(relativePoint)
    }
}