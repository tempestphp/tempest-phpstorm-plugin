package com.github.tempest.framework.php

import com.github.tempest.framework.TempestFrameworkClasses
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.Variable

fun PhpFile.getPhpViewVariables(): Set<Variable> {
    return PsiTreeUtil.findChildrenOfType(this, Variable::class.java)
        .filter { it.useScope == useScope }
        .filter { !it.isDeclaration }
        .distinctBy { it.name }
        .toSet()
}

fun Method.getConsoleCommandName(): String? {
    return this
        .getAttributes(TempestFrameworkClasses.ConsoleCommand)
        .firstOrNull()
        ?.arguments
        ?.run { this.find { it.name == "name" } ?: firstOrNull() }
        ?.argument
        ?.value
        ?.run { StringUtil.unquoteString(this) }
}

fun PhpIndex.getMethodsByFQN(fqn:String): Collection<Method> {
    val (className, methodName) = fqn.split('.')
    return this
        .getClassesByFQN(className)
        .mapNotNull{ it.findMethodByName(methodName) }
}

