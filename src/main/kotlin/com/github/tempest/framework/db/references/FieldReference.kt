package com.github.tempest.framework.db.references

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.jetbrains.php.completion.PhpLookupElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class FieldReference(
    element: StringLiteralExpression,
) : PsiPolyVariantReferenceBase<PsiElement>(element) {
    override fun getVariants(): Array<out Any?> {
        val element = element as? StringLiteralExpression ?: return emptyArray()
        return ClassTargetResolveUtils
            .resolve(element)
            .flatMap { phpClass ->
                phpClass.fields
                    .map { PhpLookupElement(it) }
            }
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult?> {
        val element = element as? StringLiteralExpression ?: return emptyArray()
        return ClassTargetResolveUtils
            .resolve(element)
            .map {phpClass->
                phpClass.fields
                    .filter { it.name == element.contents }
                    .map { it }
            }
            .flatMap { it }
            .let { PsiElementResolveResult.createResults(it) }

    }
}
