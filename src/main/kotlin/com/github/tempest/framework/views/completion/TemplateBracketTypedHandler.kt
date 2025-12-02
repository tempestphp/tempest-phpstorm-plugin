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

        if (c == '{' || c == '!' || c == '-') {
            val result = handleAutoComplete(project, editor, text, offset)
            if (result == Result.STOP) return result
        }

        if (c == '!' || c == '-') {
            synchronizeBrackets(project, editor, text, offset)
        }

        if (c == ' ') {
            handleSpaceInBrackets(project, editor, text, offset)
        }

        return Result.CONTINUE
    }

    private fun handleSpaceInBrackets(project: Project, editor: Editor, text: CharSequence, offset: Int) {
        val textBefore = text.subSequence(0, offset).toString()
        val textAfter = text.subSequence(offset, text.length).toString()

        for (pair in BRACKET_PAIRS) {
            if (textBefore.endsWith(pair.opening + " ") && textAfter.startsWith(pair.closing)) {
                WriteCommandAction.runWriteCommandAction(project) {
                    editor.document.insertString(offset, " ")
                }
                return
            }
        }
    }

    private fun handleAutoComplete(project: Project, editor: Editor, text: CharSequence, offset: Int): Result {
        if (offset < 2) return Result.CONTINUE

        val textBefore = text.subSequence(0, offset).toString()

        for (pair in AUTO_COMPLETE_PAIRS) {
            if (textBefore.endsWith(pair.opening)) {
                if (!hasAnyClosingBracketAhead(text, offset)) {
                    insertClosingBracket(project, editor, offset, pair.closing)
                    return Result.STOP
                }
            }
        }

        return Result.CONTINUE
    }

    private fun synchronizeBrackets(project: Project, editor: Editor, text: CharSequence, offset: Int) {
        val textBefore = text.subSequence(0, offset).toString()

        val currentPair = BRACKET_PAIRS.find { textBefore.endsWith(it.opening) } ?: return

        if (currentPair.opening == "{{") return

        val textAfter = text.subSequence(offset, text.length).toString()

        for (otherPair in BRACKET_PAIRS) {
            if (otherPair.closing == currentPair.closing) continue

            val closingIndex = findFirstUnnestedClosing(textAfter, otherPair.closing, currentPair.opening)
            if (closingIndex != -1) {
                replaceText(project, editor, offset + closingIndex, otherPair.closing.length, currentPair.closing)
                return
            }
        }
    }

    fun synchronizeBracketsAfterDeletion(project: Project, editor: Editor) {
        val offset = editor.caretModel.offset
        val text = editor.document.charsSequence
        val textBefore = text.subSequence(0, offset).toString()

        if (!textBefore.endsWith("{{")) return

        if (textBefore.endsWith("{{!!") || textBefore.endsWith("{{--")) return

        val textAfter = text.subSequence(offset, text.length).toString()

        for (pair in BRACKET_PAIRS) {
            if (pair.closing == "}}") continue

            val closingIndex = findFirstUnnestedClosing(textAfter, pair.closing, "{{")
            if (closingIndex != -1) {
                replaceText(project, editor, offset + closingIndex, pair.closing.length, "}}")
                return
            }
        }
    }

    private fun findFirstUnnestedClosing(textAfter: String, closing: String, opening: String): Int {
        val closingIndex = textAfter.indexOf(closing)
        if (closingIndex == -1) return -1

        val textBeforeClosing = textAfter.take(closingIndex)
        val nextOpenIndex = textBeforeClosing.indexOf(opening)

        return if (nextOpenIndex == -1) closingIndex else -1
    }

    private fun hasAnyClosingBracketAhead(text: CharSequence, offset: Int): Boolean {
        val textAfter = text.subSequence(offset, text.length).toString()

        for (pair in BRACKET_PAIRS) {
            val nextClose = textAfter.indexOf(pair.closing)
            if (nextClose != -1) {
                val nextOpen = textAfter.indexOf(pair.opening)
                if (nextOpen == -1 || nextClose < nextOpen) {
                    return true
                }
            }
        }

        return false
    }

    private fun insertClosingBracket(project: Project, editor: Editor, offset: Int, closing: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(offset, closing)
        }
    }

    private fun replaceText(project: Project, editor: Editor, start: Int, length: Int, newText: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.replaceString(start, start + length, newText)
        }
    }

    companion object {
        val INSTANCE = TemplateBracketTypedHandler()
    }
}

val BRACKET_PAIRS = listOf(
    TemplateBracketTypedHandler.BracketPair("{{--", "--}}"),
    TemplateBracketTypedHandler.BracketPair("{{!!", "!!}}"),
    TemplateBracketTypedHandler.BracketPair("{{", "}}"),
)

private val AUTO_COMPLETE_PAIRS = listOf(
    TemplateBracketTypedHandler.BracketPair("{{--", "--}}"),
    TemplateBracketTypedHandler.BracketPair("{{!!", "!!}}"),
    TemplateBracketTypedHandler.BracketPair("{{", "}}"),
)