package com.github.tempest.framework

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

class ComponentGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        element: PsiElement?, offset: Int, editor: Editor?
    ): Array<PsiElement>? {
        if (element == null) return null

        val tag = PsiTreeUtil.getParentOfType(element, HtmlTag::class.java) ?: return null
        if (!tag.name.startsWith("x-")) return null

        val project = tag.project
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val projectFiles = mutableListOf<PsiFile>()
        val libraryFiles = mutableListOf<PsiFile>()

        FilenameIndex.processFilesByName(
            tag.name + TempestFrameworkUtil.TEMPLATE_PREFIX, true, GlobalSearchScope.projectScope(project)
        ) { virtualFile ->
            val psiFile = virtualFile.findPsiFile(project) ?: return@processFilesByName true

            if (projectFileIndex.isInSourceContent(virtualFile)) {
                projectFiles.add(psiFile)
            } else {
                libraryFiles.add(psiFile)
            }

            true
        }

        return when {
            projectFiles.isNotEmpty() -> projectFiles.toTypedArray()
            libraryFiles.isNotEmpty() -> libraryFiles.toTypedArray()
            else -> null
        }
    }
}
