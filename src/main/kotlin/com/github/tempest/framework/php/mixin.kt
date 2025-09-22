package com.github.tempest.framework.php

import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.Variable

fun PhpFile.getPhpViewVariables(): Set<Variable> {
    return PsiTreeUtil.findChildrenOfType(this, Variable::class.java)
        .filter { it.useScope == useScope }
        .filter { !it.isDeclaration }
        .distinctBy { it.name }
        .toSet()
}
