package com.github.tempest.framework.common.completion

import com.intellij.codeInsight.completion.CompletionLocation
import com.intellij.codeInsight.completion.CompletionWeigher
import com.intellij.codeInsight.lookup.LookupElement

class CompletionWeighter : CompletionWeigher() {
    override fun weigh(
        element: LookupElement,
        location: CompletionLocation
    ) = when {
        element is TopPriorityLookupElement -> 1_000_000
        else -> null
    }
}