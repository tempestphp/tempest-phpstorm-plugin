package com.github.tempest.framework.scaffolder.project.override

import com.intellij.ide.util.projectWizard.ModuleNameLocationSettings
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.NewProjectWizardBaseStep
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.Panel
import com.intellij.util.PathUtil
import java.io.File
import javax.swing.JComponent

/**
 * Port of [com.intellij.ide.util.projectWizard.PanelBuilderSettingsStep]
 */
class PanelBuilderSettingsStep(
    private val wizardContext: WizardContext,
    private val builder: Panel,
    private val baseStep: NewProjectWizardBaseStep
) : SettingsStep {
    override fun getContext(): WizardContext = wizardContext

    override fun addSettingsField(label: String, field: JComponent) {
        with(builder) {
            row(label) {
                cell(field).align(AlignX.FILL)
            }.bottomGap(BottomGap.SMALL)
        }
    }

    override fun addSettingsComponent(component: JComponent) {
        with(builder) {
//            row("") { <---- seems like a bug, it adds left padding
            row {
                cell(component).align(AlignX.FILL)
            }.bottomGap(BottomGap.SMALL)
        }
    }

    override fun addExpertPanel(panel: JComponent) {
        addSettingsComponent(panel)
    }

    override fun addExpertField(label: String, field: JComponent) {
        addSettingsField(label, field)
    }

    override fun getModuleNameLocationSettings(): ModuleNameLocationSettings {
        return object : ModuleNameLocationSettings {
            override fun getModuleName(): String = baseStep.name

            override fun setModuleName(moduleName: String) {
                baseStep.name = moduleName
            }

            override fun getModuleContentRoot(): String {
                return FileUtil.toSystemDependentName(baseStep.path)
                    .trimEnd(File.separatorChar) + File.separatorChar + baseStep.name
            }

            override fun setModuleContentRoot(path: String) {
                baseStep.path = PathUtil.getParentPath(path)
                baseStep.name = PathUtil.getFileName(path)
            }
        }
    }
}