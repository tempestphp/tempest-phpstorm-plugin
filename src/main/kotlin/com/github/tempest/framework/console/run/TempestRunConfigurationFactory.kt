package com.github.tempest.framework.console.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project

class TempestRunConfigurationFactory(private val runConfigurationType: TempestConsoleCommandRunConfigurationType) :
    ConfigurationFactory(runConfigurationType) {
    override fun getId() = TempestConsoleCommandRunConfigurationType.ID
    override fun getName() = runConfigurationType.displayName

    override fun createTemplateConfiguration(project: Project) =
        TempestConsoleCommandRunConfiguration(project, this, "name")
}