package com.github.tempest.framework.router.completion

import com.github.tempest.framework.TempestFrameworkClasses
import com.github.tempest.framework.router.references.RouteLookupElementBuilder
import com.github.tempest.framework.router.references.RouteResolveUtils
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.ConstantReference
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.ParameterList

class RouteParameterCompletionContributor : CompletionContributor() {
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
                                PlatformPatterns.psiElement(FunctionReference::class.java),
                            ),
                        PlatformPatterns.psiElement(ParameterList::class.java)
                            .withParent(PlatformPatterns.psiElement(FunctionReference::class.java)),
                    )
                ),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = when (parameters.position.parent) {
                        is ConstantReference -> parameters.position.parent
                        is ParameterList -> parameters.position
                        else -> return
                    }
                    val parameterList = element.parent as? ParameterList ?: return
                    val function = parameterList.parent as? FunctionReference ?: return
                    if (function.fqn != TempestFrameworkClasses.FUNCTION_URI) return
                    if (parameterList.parameters.isEmpty()) return

                    val firstParameter = function.parameters[0]
                    if (firstParameter == element) return

                    RouteResolveUtils
                        .resolveCached(firstParameter)
                        .flatMap { RouteLookupElementBuilder.create(it) }
                        .apply { result.addAllElements(this) }
                        .apply { if (isNotEmpty()) result.stopHere() }
                }

            }
        )
    }
}