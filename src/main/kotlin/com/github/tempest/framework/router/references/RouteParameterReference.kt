package com.github.tempest.framework.router.references

import com.github.tempest.framework.php.getMethodsByFQN
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.PhpPsiElementFactory

class RouteParameterReference(
    val hostElement: PsiElement,
    val routeReferenceElement: PsiElement,
    val parameterName: String,
    textRange: TextRange,
) : PsiReferenceBase<PsiElement>(hostElement, textRange) {
    override fun resolve(): PsiElement? {
        val phpIndex = PhpIndex.getInstance(routeReferenceElement.project)
        return RouteResolveUtils
            .resolve(routeReferenceElement)
            .firstOrNull()
            ?.let { phpIndex.getMethodsByFQN(it.action) }
            ?.firstOrNull()
            ?.getParameter(parameterName)
    }

    override fun getVariants() = RouteResolveUtils
        .resolve(routeReferenceElement)
        .flatMap { RouteLookupElementBuilder.create(it) }
        .toTypedArray()

    override fun handleElementRename(newElementName: String): PsiElement? {
        return hostElement.findElementAt(rangeInElement.startOffset)
            ?.replace(
                PhpPsiElementFactory.createNamedArgumentNameIdentifier(
                    hostElement.project,
                    newElementName,
                )
            )
    }
}