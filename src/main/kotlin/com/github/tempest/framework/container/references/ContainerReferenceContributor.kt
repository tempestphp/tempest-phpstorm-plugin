package com.github.tempest.framework.container.references

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.ParameterList

class ContainerReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(ParameterList::class.java)
                .withParent(MethodReference::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val parameterList = element as? ParameterList ?: return emptyArray()
                    val function = parameterList.parent as? MethodReference ?: return emptyArray()
                    if (!ContainerPsiUtils.isInvokeMethod(function)) return emptyArray()
                    if (parameterList.parameters.isEmpty()) return emptyArray()

                    val firstParameter = function.parameters[0]
                    if (firstParameter == element) return emptyArray()

                    return parameterList
                        .node
                        .getChildren(IDENTIFIER_TOKENS)
                        .map {
                            ContainerParameterReference(
                                element,
                                firstParameter,
                                it.text,
                                it.textRange.shiftLeft(element.startOffset),
                            )
                        }
                        .toTypedArray()
//                        .apply { println("found references for ${function.name} ${this.joinToString { it.toString() }}") }
                }
            }
        )
    }

    companion object {
        val IDENTIFIER_TOKENS = TokenSet.create(PhpTokenTypes.IDENTIFIER)
    }
}