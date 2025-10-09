package com.github.tempest.framework.common.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementDecorator

class TopPriorityLookupElement(myDelegate: LookupElement) : LookupElementDecorator<LookupElement>(myDelegate)