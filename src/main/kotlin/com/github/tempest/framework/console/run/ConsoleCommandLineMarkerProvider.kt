package com.github.tempest.framework.console.run

import com.github.tempest.framework.TempestBundle
import com.github.tempest.framework.php.getConsoleCommandName
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method

class ConsoleCommandLineMarkerProvider : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement) = when {
        element.parent !is Method -> null
        (element.parent as? Method)?.nameIdentifier != element -> null
        else -> {
            val commandName = (element.parent as? Method)?.getConsoleCommandName() ?: return null
            Info(
                AllIcons.Actions.Execute,
                ExecutorAction.getActions(1),
            ) {
                TempestBundle.message(
                    "action.run.target.text",
                    StringUtil.wrapWithDoubleQuote(TempestBundle.message("action.run.target.command", commandName)),
                )
            }
        }
    }
}