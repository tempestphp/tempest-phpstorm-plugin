package com.github.tempest.framework.views.completion

import com.github.tempest.framework.TempestFrameworkUtil
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class TemplateBracketTypedHandler : TypedHandlerDelegate() {

    data class BracketPair(val opening: String, val closing: String)

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (!file.name.endsWith(TempestFrameworkUtil.TEMPLATE_SUFFIX)) return Result.CONTINUE

        val offset = editor.caretModel.offset
        val text = editor.document.charsSequence

        return when (c) {
            '{' -> {
                insertClosingBracket(project, editor, offset, "}")
                Result.STOP
            }
            '!', '-' -> {
                if (text.matchesAt(offset - 3, DOUBLE_BRACE_OPEN)) {
                    handleDoubledSpecialChar(project, editor, offset, c)
                    Result.STOP
                } else {
                    Result.CONTINUE
                }
            }
            ' ' -> {
                handleSpaceInBrackets(project, editor, text, offset)
                Result.CONTINUE
            }
            else -> Result.CONTINUE
        }
    }

    private fun handleDoubledSpecialChar(project: Project, editor: Editor, offset: Int, char: Char) {
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(offset, char.toString())
            editor.caretModel.moveToOffset(offset + 1)
            transformClosingBracket(editor, offset + 1, char)
        }
    }

    private fun handleSpaceInBrackets(project: Project, editor: Editor, text: CharSequence, offset: Int) {
        BRACKET_PAIRS.firstOrNull { pair ->
            text.matchesAt(offset - pair.opening.length - 1, pair.opening) &&
            text[offset - 1] == ' ' &&
            text.matchesAt(offset, pair.closing)
        }?.let {
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.insertString(offset, " ")
            }
        }
    }

    private fun transformClosingBracket(editor: Editor, offset: Int, char: Char) {
        val text = editor.document.charsSequence
        val newClosing = "$char$char$DOUBLE_BRACE_CLOSE"

        findNextUnnestedClosing(text, offset, DOUBLE_BRACE_CLOSE)?.let { closingIndex ->
            editor.document.replaceString(closingIndex, closingIndex + 2, newClosing)
        }
    }

    fun synchronizeBracketsAfterDeletion(project: Project, editor: Editor, deletedChar: Char) {
        val offset = editor.caretModel.offset
        val text = editor.document.charsSequence

        WriteCommandAction.runWriteCommandAction(project) {
            when {
                text.matchesAt(offset - 3, DOUBLE_BRACE_OPEN) && text.getOrNull(offset - 1) == deletedChar -> {
                    editor.document.deleteString(offset - 1, offset)
                    transformClosingAfterDeletion(editor, offset - 1, deletedChar)
                }
                text.matchesAt(offset - 2, DOUBLE_BRACE_OPEN) && text.getOrNull(offset) == deletedChar -> {
                    editor.document.deleteString(offset, offset + 1)
                    transformClosingAfterDeletion(editor, offset, deletedChar)
                }
            }
        }
    }

    private fun transformClosingAfterDeletion(editor: Editor, offset: Int, char: Char) {
        val text = editor.document.charsSequence
        val doubledClosing = "$char$char$DOUBLE_BRACE_CLOSE"

        findNextUnnestedClosing(text, offset, doubledClosing)?.let { closingIndex ->
            editor.document.replaceString(closingIndex, closingIndex + 4, DOUBLE_BRACE_CLOSE)
        }
    }

    private fun insertClosingBracket(project: Project, editor: Editor, offset: Int, closing: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(offset, closing)
        }
    }

    private fun findNextUnnestedClosing(text: CharSequence, startOffset: Int, pattern: String): Int? {
        val patternLength = pattern.length
        val searchRange = startOffset..(text.length - patternLength)

        for (i in searchRange) {
            if (text.matchesAt(i, pattern) && !hasOpeningBetween(text, startOffset, i)) {
                return i
            }
        }
        return null
    }

    private fun hasOpeningBetween(text: CharSequence, start: Int, end: Int): Boolean {
        return (start until end - 1).any { i ->
            text.matchesAt(i, DOUBLE_BRACE_OPEN)
        }
    }

    companion object {
        val INSTANCE = TemplateBracketTypedHandler()

        private const val DOUBLE_BRACE_OPEN = "{{"
        private const val DOUBLE_BRACE_CLOSE = "}}"
    }
}

private fun CharSequence.matchesAt(index: Int, pattern: String): Boolean {
    if (index < 0 || index + pattern.length > length) return false
    return (pattern.indices).all { i -> this[index + i] == pattern[i] }
}

val BRACKET_PAIRS = listOf(
    TemplateBracketTypedHandler.BracketPair("{{--", "--}}"),
    TemplateBracketTypedHandler.BracketPair("{{!!", "!!}}"),
    TemplateBracketTypedHandler.BracketPair("{{", "}}"),
)