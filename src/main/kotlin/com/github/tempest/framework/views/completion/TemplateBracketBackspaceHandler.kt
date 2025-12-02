package com.github.tempest.framework.views.completion

import com.github.tempest.framework.TempestFrameworkUtil
import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class TemplateBracketBackspaceHandler : BackspaceHandlerDelegate() {

    override fun beforeCharDeleted(c: Char, file: PsiFile, editor: Editor) = Unit

    override fun charDeleted(c: Char, file: PsiFile, editor: Editor): Boolean {
        if (!file.name.endsWith(TempestFrameworkUtil.TEMPLATE_SUFFIX) || c !in SPECIAL_CHARS) {
            return false
        }

        TemplateBracketTypedHandler.INSTANCE.synchronizeBracketsAfterDeletion(file.project, editor, c)
        return false
    }

    private companion object {
        val SPECIAL_CHARS = setOf('!', '-')
    }
}
