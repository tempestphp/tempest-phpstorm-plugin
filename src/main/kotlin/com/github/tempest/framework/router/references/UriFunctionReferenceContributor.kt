package com.github.tempest.framework.router.references

import com.github.tempest.framework.TempestFrameworkClasses
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.impl.source.tree.injected.changesHandler.contentRange
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class UriFunctionReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withParent(PlatformPatterns.psiElement(ParameterList::class.java))
                .withSuperParent(
                    2,
                    PlatformPatterns.psiElement(FunctionReference::class.java)
                ),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val element = element as? StringLiteralExpression ?: return emptyArray()
                    val parameterList = element.parent as? ParameterList ?: return emptyArray()
                    val function = parameterList.parent as? FunctionReference ?: return emptyArray()
                    if (function.fqn != TempestFrameworkClasses.FUNCTION_URI) return emptyArray()
                    if (parameterList.parameters.isEmpty()) return emptyArray()
                    if (parameterList.parameters[0] != element) return emptyArray()

                    return arrayOf(
                        RouteReference(
                            element,
                            element.contentRange.shiftLeft(element.startOffset),
                        )
                    )
                }
            }
        )
    }
}