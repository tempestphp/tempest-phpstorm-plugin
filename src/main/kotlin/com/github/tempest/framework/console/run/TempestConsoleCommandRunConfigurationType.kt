package com.github.tempest.framework.console.run

import com.github.tempest.framework.TempestIcons
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.project.Project

class TempestConsoleCommandRunConfigurationType : ConfigurationTypeBase(
    ID,
    "Tempest Command",
    "Runs console command",
    TempestIcons.TEMPEST,
) {
    init {
        addFactory(object : ConfigurationFactory(this) {
            override fun getId() = ID

            override fun createTemplateConfiguration(project: Project) =
                TempestConsoleCommandRunConfiguration(project, this, "Tempest")

            override fun getOptionsClass() = TempestConsoleCommandRunConfigurationSettings::class.java
        })
    }

    companion object {
        const val ID = "TempestConsoleCommandRunConfiguration"

        val INSTANCE = TempestConsoleCommandRunConfigurationType()
    }
}