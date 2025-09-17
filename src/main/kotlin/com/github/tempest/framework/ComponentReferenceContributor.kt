package com.github.tempest.framework

import com.intellij.openapi.vfs.findPsiFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlChildRole
import com.intellij.util.ProcessingContext

private const val TEMPLATE_PREFIX = ".view.php"

class ComponentReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HtmlTag::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    if (element !is HtmlTag) return emptyArray()
                    if (!element.name.startsWith("x-")) return emptyArray()

                    val nameElement = XmlChildRole.START_TAG_NAME_FINDER.findChild(element.node) ?: return emptyArray()

                    val project = element.project

                    val result = mutableListOf<PsiReference>()

                    FilenameIndex.processFilesByName(
                        element.name + TEMPLATE_PREFIX,
                        true,
                        GlobalSearchScope.projectScope(project),
                        {
                            val psiFile = it.findPsiFile(project) ?: return@processFilesByName true
                            result.add(
                                object : PsiReferenceBase<PsiElement>(element, nameElement.textRange, false) {
                                    override fun resolve() = psiFile
                                }
                            )

                            true
                        })

                    return result.toTypedArray()
                }
            }
        )
    }
}