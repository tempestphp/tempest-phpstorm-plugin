package com.github.tempest.framework.console.run

import com.github.tempest.framework.TempestBundle
import com.github.tempest.framework.php.getConsoleCommandName
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method

class TempestRunConfigurationProducer : LazyRunConfigurationProducer<TempestConsoleCommandRunConfiguration>() {
    override fun setupConfigurationFromContext(
        configuration: TempestConsoleCommandRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val element = context.psiLocation as? Method ?: return false
        val commandName = element.getConsoleCommandName() ?: return false

        configuration.settings.commandName = commandName
        configuration.name = TempestBundle.message("action.run.target.command", commandName)

        return true
    }

    override fun isConfigurationFromContext(
        configuration: TempestConsoleCommandRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val method = context.psiLocation as? Method ?: return false

        return configuration.settings.commandName == method.getConsoleCommandName()
    }

    override fun getConfigurationFactory() =
        TempestRunConfigurationFactory(TempestConsoleCommandRunConfigurationType.INSTANCE)
}
