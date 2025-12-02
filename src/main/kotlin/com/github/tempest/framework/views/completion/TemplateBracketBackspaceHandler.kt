package com.github.tempest.framework.views.completion

import com.github.tempest.framework.TempestFrameworkUtil
import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class TemplateBracketBackspaceHandler : BackspaceHandlerDelegate() {

    override fun beforeCharDeleted(c: Char, file: PsiFile, editor: Editor) {}

    override fun charDeleted(c: Char, file: PsiFile, editor: Editor): Boolean {
        if (!file.name.endsWith(TempestFrameworkUtil.TEMPLATE_SUFFIX)) return false

        if (c != '!' && c != '-') return false

        TemplateBracketTypedHandler.INSTANCE.synchronizeBracketsAfterDeletion(file.project, editor)

        return false
    }
}
