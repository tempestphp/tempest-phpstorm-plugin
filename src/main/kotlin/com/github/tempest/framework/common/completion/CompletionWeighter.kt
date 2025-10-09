package com.github.tempest.framework.common.completion

import com.intellij.codeInsight.completion.CompletionLocation
import com.intellij.codeInsight.completion.CompletionWeigher
import com.intellij.codeInsight.lookup.LookupElement

class CompletionWeighter : CompletionWeigher() {
    override fun weigh(
        element: LookupElement,
        location: CompletionLocation
    ): Comparable<*>? {
        println("completion weigher: $element")
        return when {
            element is TopPriorityLookupElement -> 10
            else -> null
        }.apply { println("weigh: $this") }
    }
}