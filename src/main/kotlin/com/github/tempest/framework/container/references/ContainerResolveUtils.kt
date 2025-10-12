package com.github.tempest.framework.container.references

import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.jetbrains.php.lang.PhpReferenceContributor
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.Function

object ContainerResolveUtils {
    fun resolveCached(firstParameter: PsiElement): Collection<Function> =
        CachedValuesManager.getCachedValue(firstParameter) {
            CachedValueProvider.Result.create(
                resolve(firstParameter),
                firstParameter.containingFile,
            )
        }

    fun resolve(firstParameter: PsiElement): Collection<Function> =
        when (firstParameter) {
            is ArrayCreationExpression -> fromArrayCreation(firstParameter)
            else -> emptyList()
        }


    fun fromArrayCreation(firstParameter: ArrayCreationExpression) =
        PhpReferenceContributor
            .getCallbackRefFromArray(firstParameter)
            ?.multiResolve(true)
            ?.mapNotNull { it.element as? Function }
            ?: emptyList()
}