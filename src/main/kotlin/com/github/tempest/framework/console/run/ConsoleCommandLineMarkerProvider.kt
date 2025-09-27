package com.github.tempest.framework.console.run

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method

class ConsoleCommandLineMarkerProvider : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement) = when {
        element !is Method -> null
        else -> Info(TempestRunTargetAction(element))
    }
}