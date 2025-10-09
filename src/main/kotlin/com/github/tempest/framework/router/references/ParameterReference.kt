package com.github.tempest.framework.router.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.jetbrains.php.completion.PhpLookupElement
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class ParameterReference(
    element: StringLiteralExpression,
    textRange: TextRange,
    val parameters: Array<Parameter>,
    val resolvedParameter: Parameter? = null
) : PsiReferenceBase<PsiElement>(element, textRange) {
    override fun resolve() = resolvedParameter

    override fun getVariants() =
        parameters
            .map { PhpLookupElement(it) }
            .toTypedArray()
}