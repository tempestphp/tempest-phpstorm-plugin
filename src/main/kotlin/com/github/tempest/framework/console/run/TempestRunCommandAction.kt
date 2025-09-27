package com.github.tempest.framework.console.run

import com.github.tempest.framework.TempestBundle
import com.intellij.execution.Executor
import com.intellij.execution.RunManagerEx
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class TempestRunCommandAction(val commandName: String) : AnAction() {
    init {
        templatePresentation.setText(TempestBundle.message("action.run.target.text", commandName), false)
        templatePresentation.description = TempestBundle.message("action.run.target.description", commandName)
        templatePresentation.icon = AllIcons.Actions.Execute
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        val runManager = RunManagerEx.getInstanceEx(project)
        val producer = TempestRunConfigurationProducer()
        val configurationFactory = producer.configurationFactory

        val runConfiguration = TempestConsoleCommandRunConfiguration(
            project,
            configurationFactory,
            TempestBundle.message("action.run.target.command", commandName),
        )
            .apply { settings.commandName = commandName }

        val configuration = runManager.createConfiguration(runConfiguration, configurationFactory)

        runManager.setTemporaryConfiguration(configuration)
        ExecutionUtil.runConfiguration(configuration, Executor.EXECUTOR_EXTENSION_NAME.extensionList.first())
    }
}