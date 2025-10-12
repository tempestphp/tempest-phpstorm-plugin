package com.github.tempest.framework.container.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.jetbrains.php.lang.psi.PhpPsiElementFactory

class ContainerParameterReference(
    val hostElement: PsiElement,
    val routeReferenceElement: PsiElement,
    val parameterName: String,
    textRange: TextRange,
) : PsiReferenceBase<PsiElement>(hostElement, textRange) {
    override fun resolve() = ContainerResolveUtils
        .resolveCached(routeReferenceElement)
        .firstOrNull()
        ?.getParameter(parameterName)

    override fun getVariants() = ContainerResolveUtils
        .resolveCached(routeReferenceElement)
        .flatMap { ContainerParameterLookupElementBuilder.create(it) }
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