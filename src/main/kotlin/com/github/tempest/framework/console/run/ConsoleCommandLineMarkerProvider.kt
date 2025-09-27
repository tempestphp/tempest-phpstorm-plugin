package com.github.tempest.framework.console.run

import com.github.tempest.framework.php.getConsoleCommandName
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method

class ConsoleCommandLineMarkerProvider : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement) = when {
        element !is Method -> null
        element.getConsoleCommandName() == null -> null
        else -> Info(TempestRunTargetAction(element))
    }
}