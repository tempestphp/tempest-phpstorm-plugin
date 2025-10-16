package com.github.tempest.framework.scaffolder.project.override

import com.github.tempest.framework.scaffolder.project.TempestProjectGenerator
import com.github.tempest.framework.scaffolder.project.TempestProjectGeneratorSettings
import com.intellij.ide.highlighter.ModuleFileType
import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardBaseStep
import com.intellij.openapi.module.WebModuleBuilder
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import java.nio.file.Path

class WebTemplateProjectWizardStep(
    val parentStep: NewProjectWizardBaseStep,
    val template: TempestProjectGenerator,
) : AbstractNewProjectWizardStep(parentStep) {
    val peer = template.createLazyPeer()

    override fun setupUI(builder: Panel) {
        peer.value.buildUI(PanelBuilderSettingsStep(parentStep.context, builder, parentStep))
    }

    override fun setupProject(project: Project) {
        webModuleBuilder().commitModule(project, null)
    }

    private fun webModuleBuilder(): WebModuleBuilder<TempestProjectGeneratorSettings> {
        val moduleName = parentStep.name
        val projectPath = Path.of(parentStep.path, moduleName)

        return WebModuleBuilder(template, peer).apply {
            contentEntryPath = projectPath.toString()
            moduleFilePath = projectPath.resolve(moduleName + ModuleFileType.DOT_DEFAULT_EXTENSION).toString()
            name = moduleName
        }
    }
}