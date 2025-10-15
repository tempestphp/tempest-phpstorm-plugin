package com.github.tempest.framework.db.references

import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.findParentOfType
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

object ClassTargetResolveUtils {
    fun resolveCached(firstParameter: StringLiteralExpression): Collection<PhpClass> =
        CachedValuesManager.getCachedValue(firstParameter) {
            CachedValueProvider.Result.create(
                resolve(firstParameter),
                firstParameter.containingFile,
            )
        }


    fun resolve(element: StringLiteralExpression): Collection<PhpClass> {
        val methodReference = element.findParentOfType<MethodReference>() ?: return emptyList()

        val methodReferenceType = methodReference.declaredType
        if (methodReferenceType.isEmpty) return emptyList()

        val modelType = methodReferenceType.typesWithParametrisedParts.find { it.contains("\\Tempest\\Database\\query)(\\") }
        val modelClass = Regex("\\\\Tempest\\\\Database\\\\query\\)\\((.+?)\\)")
            .find(modelType ?: "")
            ?.groupValues[1]
            ?: return emptyList()

        return PhpIndex
            .getInstance(element.project)
            .getAnyByFQN(modelClass)
    }
}