package com.github.tempest.framework.console.run

import com.github.tempest.framework.TempestBundle
import com.intellij.execution.Executor
import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.RunManagerEx
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.jetbrains.php.lang.psi.elements.Method

class TempestRunTargetAction(private val target: Method) : AnAction() {
    init {
        templatePresentation.setText(TempestBundle.message("action.run.target.text", target.name), false)
        templatePresentation.description = TempestBundle.message("action.run.target.description", target.name)
        templatePresentation.icon = AllIcons.Actions.Execute
    }

    override fun actionPerformed(event: AnActionEvent) {
        val dataContext = SimpleDataContext.getSimpleContext(Location.DATA_KEY, PsiLocation(target), event.dataContext)

        val context = ConfigurationContext.getFromContext(dataContext, event.place)

        val producer = TempestRunConfigurationProducer()
        val configuration = producer.findOrCreateConfigurationFromContext(context)?.configurationSettings ?: return

        (context.runManager as RunManagerEx).setTemporaryConfiguration(configuration)
        ExecutionUtil.runConfiguration(configuration, Executor.EXECUTOR_EXTENSION_NAME.extensionList.first())
    }
}