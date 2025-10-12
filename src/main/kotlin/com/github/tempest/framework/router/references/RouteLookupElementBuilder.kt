package com.github.tempest.framework.router.references

import com.github.tempest.framework.common.completion.TopPriorityLookupElement
import com.github.tempest.framework.common.insertHandler.InsertTextInsertHandler
import com.github.tempest.framework.router.index.Route
import com.intellij.codeInsight.completion.DeclarativeInsertHandler
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.jetbrains.php.PhpIcons

object RouteLookupElementBuilder {
    fun create(route: Route): Collection<LookupElement> {
        return route
            .parameters
            .map { parameter ->
                LookupElementBuilder.create(parameter.name)
                    .withIcon(PhpIcons.PARAMETER)
                    .withTailText(" Pattern: ${parameter.pattern}".takeIf { parameter.pattern.isNotEmpty() })
                    .withTypeText(route.pattern)
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
    }
}