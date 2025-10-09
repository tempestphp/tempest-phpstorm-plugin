package com.github.tempest.framework.router.references

import com.github.tempest.framework.TempestFrameworkClasses
import com.github.tempest.framework.router.index.Route
import com.github.tempest.framework.router.index.RouterIndexUtils
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.PhpReferenceContributor
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

object RouteResolveUtils {
    fun resolve(firstParameter: PsiElement): Collection<Route> =
        when (firstParameter) {
            is ArrayCreationExpression -> fromArrayCreation(firstParameter)
            is StringLiteralExpression -> fromStringLiteral(firstParameter)
            else -> emptyList()
        }


    fun fromArrayCreation(firstParameter: ArrayCreationExpression) =
        PhpReferenceContributor
            .getCallbackRefFromArray(firstParameter)
            ?.resolve()
            ?.let { it as? Method }
            ?.attributes
            ?.filter { it.fqn in TempestFrameworkClasses.ROUTES }
            ?.mapNotNull { RouterIndexUtils.createRouteFromAttribute(it) }
            ?: emptyList()

    fun fromStringLiteral(routePattern: StringLiteralExpression) =
        RouterIndexUtils
            .getRoutesByPattern(routePattern.contents, routePattern.project)

}