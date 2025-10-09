package com.github.tempest.framework.common.insertHandler

import com.intellij.codeInsight.completion.DeclarativeInsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange

class InsertTextInsertHandler(
    private val stringToInsert: String,
    popupOptions: PopupOptions
) : DeclarativeInsertHandler(
    listOf(RelativeTextEdit(0, 0, stringToInsert)),
    stringToInsert.length,
    null,
    null,
    popupOptions
) {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val applyTextOperations = !isValueAlreadyHere(context.editor)
        conditionalHandleInsert(context, item, applyTextOperations)
    }

    private fun isValueAlreadyHere(editor: Editor): Boolean {
        val startOffset = editor.caretModel.offset
        val valueLength = stringToInsert.length
        return editor.document.textLength >= startOffset + valueLength &&
                editor.document.getText(TextRange.create(startOffset, startOffset + valueLength)) == stringToInsert
    }
}