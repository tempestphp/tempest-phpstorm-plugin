package com.github.tempest.framework.router.references

import com.github.tempest.framework.TempestIcons
import com.github.tempest.framework.php.getMethodsByFQN
import com.github.tempest.framework.router.index.RouterIndexUtils
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.jetbrains.php.PhpIndexImpl
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class RouteReference(
    val myElement: StringLiteralExpression,
    textRange: TextRange,
) : PsiPolyVariantReferenceBase<PsiElement>(myElement, textRange) {
    override fun getVariants() =
        RouterIndexUtils
            .getAllRoutes(element.project)
            .map { route ->
                val methodFqn = PhpLangUtil.toPresentableFQN(route.action.replace('.', ':'))
                LookupElementBuilder
                    .create(route.pattern)
                    .withIcon(TempestIcons.TEMPEST)
                    .withTailText(" [$methodFqn]")
                    .withTypeText(route.method)
            }
            .toTypedArray()

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult?> {
        val project = element.project
        val phpIndex = PhpIndexImpl.getInstance(project)

        return RouterIndexUtils
            .getRoutesByPattern(myElement.contents, project)
            .flatMap { phpIndex.getMethodsByFQN(it.action) }
            .let { PsiElementResolveResult.createResults(it) }
    }
}