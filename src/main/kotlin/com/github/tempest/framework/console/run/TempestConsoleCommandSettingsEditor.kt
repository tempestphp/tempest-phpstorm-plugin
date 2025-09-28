package com.github.tempest.framework.console.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.php.run.PhpCommandLineConfigurationEditor
import javax.swing.JPanel
import javax.swing.JTextField

private fun PhpCommandLineConfigurationEditor.getMainPanel(): JPanel? {
    val reflection = PhpCommandLineConfigurationEditor::class.java.getDeclaredField("myMainPanel")
    reflection.isAccessible = true
    return reflection.get(this) as JPanel?
}

class TempestConsoleCommandSettingsEditor private constructor(): SettingsEditor<TempestConsoleCommandRunConfiguration>() {
    private val commandNameField = JTextField()
    private val phpCommandLineConfigurationEditor = PhpCommandLineConfigurationEditor()

    private lateinit var myPanel: DialogPanel

    constructor(project: com.intellij.openapi.project.Project) : this() {
        myPanel = panel {
            row {
                cell(commandNameField)
                    .label("Command:", LabelPosition.LEFT)
                    .align(Align.FILL)
            }.topGap(TopGap.MEDIUM)

            row {
                phpCommandLineConfigurationEditor.init(project, true)
                phpCommandLineConfigurationEditor.getMainPanel()?.apply {
                    scrollCell(this).align(Align.FILL)
                }
            }.topGap(TopGap.MEDIUM)
        }
    }

    override fun resetEditorFrom(tempestConsoleCommandRunConfiguration: TempestConsoleCommandRunConfiguration) {
        val settings = tempestConsoleCommandRunConfiguration.settings

        myPanel.reset()
        commandNameField.text = settings.commandName
        phpCommandLineConfigurationEditor.resetEditorFrom(settings.commandLineSettings)
    }

    override fun applyEditorTo(tempestConsoleCommandRunConfiguration: TempestConsoleCommandRunConfiguration) {
        val settings = tempestConsoleCommandRunConfiguration.settings

        settings.commandName = commandNameField.text
        phpCommandLineConfigurationEditor.applyEditorTo(settings.commandLineSettings)
    }

    override fun createEditor() = myPanel
}