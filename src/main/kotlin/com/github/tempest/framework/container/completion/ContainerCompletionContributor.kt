package com.github.tempest.framework.container.completion

import com.github.tempest.framework.container.references.ContainerParameterLookupElementBuilder
import com.github.tempest.framework.container.references.ContainerPsiUtils
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.ConstantReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.ParameterList

class ContainerCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withParent(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(ConstantReference::class.java)
                            .withParent(PlatformPatterns.psiElement(ParameterList::class.java))
                            .withSuperParent(
                                2,
                                PlatformPatterns.psiElement(MethodReference::class.java),
                            ),
                        PlatformPatterns.psiElement(ParameterList::class.java)
                            .withParent(PlatformPatterns.psiElement(MethodReference::class.java)),
                    )
                ),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
//                    println("container completion: ${parameters.position}")
                    val element = when (parameters.position.parent) {
                        is ConstantReference -> parameters.position.parent
                        is ParameterList -> parameters.position
                        else -> return
                    }
                    val parameterList = element.parent as? ParameterList ?: return
                    val function = parameterList.parent as? MethodReference ?: return
                    if (!ContainerPsiUtils.isInvokeMethod(function)) return
                    if (parameterList.parameters.isEmpty()) return

                    val firstParameter = function.parameters[0]
                    if (firstParameter == element) return

                    com.github.tempest.framework.container.references.ContainerResolveUtils
                        .resolveCached(firstParameter)
                        .flatMap { ContainerParameterLookupElementBuilder.create(it) }
                        .apply { result.addAllElements(this) }
                        .apply { if (isNotEmpty()) result.stopHere() }
                }

            }
        )
    }
}