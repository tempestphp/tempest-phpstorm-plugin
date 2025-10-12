package com.github.tempest.framework.container.references

import com.github.tempest.framework.common.completion.TopPriorityLookupElement
import com.github.tempest.framework.common.insertHandler.InsertTextInsertHandler
import com.intellij.codeInsight.completion.DeclarativeInsertHandler
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.jetbrains.php.PhpIcons
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.psi.elements.Function

object ContainerParameterLookupElementBuilder {
    fun create(target: Function) = target
        .parameters
        .map { parameter ->
            val methodFqn = PhpLangUtil.toPresentableFQN(target.fqn.replace('.', ':'))
            LookupElementBuilder.create(parameter.name)
                .withIcon(PhpIcons.PARAMETER)
                .withTailText(" [$methodFqn]")
                .withTypeText(parameter.text)
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