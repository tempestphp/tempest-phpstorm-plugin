package com.github.tempest.framework.router.completion

import com.github.tempest.framework.TempestFrameworkClasses
import com.github.tempest.framework.common.completion.TopPriorityLookupElement
import com.github.tempest.framework.common.insertHandler.InsertTextInsertHandler
import com.github.tempest.framework.router.index.RouterIndexUtils
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.DeclarativeInsertHandler
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.jetbrains.php.PhpIcons
import com.jetbrains.php.lang.psi.elements.ConstantReference
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

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
                    if (parameterList.parameters[0] == element) return

                    val routePattern = function.parameters[0] as? StringLiteralExpression ?: return

                    RouterIndexUtils.getRoutes(routePattern.contents, element.project)
                        .flatMap { it.parameters }
                        .map { parameter ->
                            LookupElementBuilder.create(parameter.name)
                                .withIcon(PhpIcons.PARAMETER)
                                .withTailText(" Pattern: ${parameter.pattern}".takeIf { parameter.pattern.isNotEmpty() })
                                .withTypeText(routePattern.contents)
                                .withInsertHandler { context, element ->
                                    InsertTextInsertHandler(
                                        ": ",
                                        DeclarativeInsertHandler.PopupOptions.MemberLookup
                                    )
                                        .handleInsert(context, element)
                                }
                                .let { PrioritizedLookupElement.withPriority(it, 10000.0) }
                                .let { PrioritizedLookupElement.withExplicitProximity(it, 10000) }
                                .let { TopPriorityLookupElement(it) }
                        }
                        .apply { result.addAllElements(this) }
                        .apply { if (isNotEmpty()) result.stopHere() }
                }
            }
        )
    }
}