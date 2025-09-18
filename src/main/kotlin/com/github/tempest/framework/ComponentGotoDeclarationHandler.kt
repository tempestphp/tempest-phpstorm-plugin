package com.github.tempest.framework

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.html.HtmlTag

class ComponentGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (element == null) return null

        val tag = PsiTreeUtil.getParentOfType(element, HtmlTag::class.java) ?: return null
        if (!tag.name.startsWith("x-")) return null

        val project = tag.project
        val result = mutableListOf<PsiElement>()

        FilenameIndex.processFilesByName(
            tag.name + TempestFrameworkUtil.TEMPLATE_PREFIX,
            true,
            GlobalSearchScope.projectScope(project),
            {
                val psiFile = it.findPsiFile(project) ?: return@processFilesByName true
                result.add(psiFile)

                true
            }
        )

        if (result.isEmpty()) return null

        return result.toTypedArray()
    }
}
