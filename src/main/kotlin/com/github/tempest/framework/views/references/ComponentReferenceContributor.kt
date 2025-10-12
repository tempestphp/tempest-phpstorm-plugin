package com.github.tempest.framework.views.references

import com.github.tempest.framework.TempestFrameworkUtil
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlChildRole
import com.intellij.util.ProcessingContext

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
                    if (!element.name.startsWith(TempestFrameworkUtil.COMPONENT_NAME_PREFIX)) return emptyArray()

                    val nameElement = XmlChildRole.START_TAG_NAME_FINDER.findChild(element.node) ?: return emptyArray()
                    val range = nameElement.textRange.shiftLeft(element.textRange.startOffset)

                    val project = element.project

                    val filename = element.name + TempestFrameworkUtil.TEMPLATE_SUFFIX
//                    println("looking for $filename")

                    val resultProject = mutableListOf<PsiReference>()
                    val resultOther = mutableListOf<PsiReference>()

                    val allScope = GlobalSearchScope.allScope(project)
                    val projectScope = GlobalSearchScope.projectScope(project)
                    FilenameIndex.processFilesByName(
                        filename,
                        true,
                        allScope,
                        {
                            val psiFile = it.findPsiFile(project) ?: return@processFilesByName true
                            if (projectScope.contains(it)) {
                                resultProject.add(PsiReferenceBase.createSelfReference(element, range, psiFile))
                            } else {
//                            println("found file $it for ${element.name}, range ${range}")
                                resultOther.add(PsiReferenceBase.createSelfReference(element, range, psiFile))
                            }

                            true
                        })

                    return listOf(resultProject, resultOther).flatten().toTypedArray()
//                        .apply { println("found references for ${element.name} ${this.joinToString { it.toString() }}") }
                }

            }
        )

        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttribute(),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val attribute = element as? XmlAttribute ?: return emptyArray()
                    val htmlTag = attribute.parent as? HtmlTag ?: return emptyArray()

                    return arrayOf(TempestAttributeReference(element, htmlTag))
//                        .apply { println("found references for ${element} ${this.joinToString { it.toString() }}") }
                }
            },
            PsiReferenceRegistrar.HIGHER_PRIORITY,
        )
    }
}