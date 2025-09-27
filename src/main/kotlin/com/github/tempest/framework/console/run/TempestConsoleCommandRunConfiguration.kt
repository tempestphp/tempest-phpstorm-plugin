package com.github.tempest.framework.console.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.project.Project
import com.jetbrains.php.config.commandLine.PhpCommandSettings
import com.jetbrains.php.run.PhpCommandLineRunConfiguration

class TempestConsoleCommandRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : PhpCommandLineRunConfiguration<TempestConsoleCommandRunConfigurationSettings>(project, factory, name) {
    override fun fillCommandSettings(
        envs: Map<String, String>,
        command: PhpCommandSettings
    ) {
        val commandName = settings.commandName ?: return
        command.setScript("tempest", false)
        command.addArgument(commandName)

        command.importCommandLineSettings(settings.commandLineSettings, command.workingDirectory)
        command.addEnvs(envs)
    }

    override fun getOptions() = super.getOptions() as TempestConsoleCommandRunConfigurationSettings
    override fun getOptionsClass(): Class<out RunConfigurationOptions> {
        return TempestConsoleCommandRunConfigurationSettings::class.java
    }

    override fun getConfigurationEditor() = TempestConsoleCommandSettingsEditor(project)

    override fun createSettings() = TempestConsoleCommandRunConfigurationSettings().apply {
    }
}