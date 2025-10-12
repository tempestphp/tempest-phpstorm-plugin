package com.github.tempest.framework.container.references

import com.github.tempest.framework.TempestFrameworkClasses
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.MethodReference

object ContainerPsiUtils {
    fun isInvokeMethod(element: PsiElement): Boolean {
        val element = element as? MethodReference ?: return false
        return element.name == "invoke" && element.signature == "#M#C${TempestFrameworkClasses.CONTAINER}.invoke"
    }
}