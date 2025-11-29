package com.github.tempest.framework.views.completion

import com.github.tempest.framework.TempestFrameworkUtil
import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class ViewVariableTypedHandler : TypedHandlerDelegate() {
    override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (charTyped != '$') return Result.CONTINUE
        if (!file.name.endsWith(TempestFrameworkUtil.TEMPLATE_SUFFIX)) return Result.CONTINUE

        val offset = editor.caretModel.offset
        val text = editor.document.charsSequence

        if (isInsideTemplateTag(text, offset)) {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            return Result.STOP
        }

        return Result.CONTINUE
    }

    private fun isInsideTemplateTag(text: CharSequence, offset: Int): Boolean {
        val textBefore = text.subSequence(0, offset).toString()

        val lastRawOpen = textBefore.lastIndexOf("{!!")
        val lastRawClose = textBefore.lastIndexOf("!!}")
        val lastEscapedOpen = textBefore.lastIndexOf("{{")
        val lastEscapedClose = textBefore.lastIndexOf("}}")

        val inRawTag = lastRawOpen > lastRawClose && lastRawOpen >= 0
        val inEscapedTag = lastEscapedOpen > lastEscapedClose && lastEscapedOpen >= 0

        if (!inRawTag && !inEscapedTag) return false

        val textAfter = text.subSequence(offset, text.length).toString()
        val expectedCloseTag = if (inRawTag && lastRawOpen > lastEscapedOpen) "!!}" else "}}"

        return textAfter.contains(expectedCloseTag)
    }
}
