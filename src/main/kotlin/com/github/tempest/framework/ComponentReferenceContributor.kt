package com.github.tempest.framework

import com.github.tempest.framework.php.getPhpViewVariables
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.XmlAttributeInsertHandler
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.PsiReferenceRegistrar.HIGHER_PRIORITY
import com.intellij.psi.ResolveResult
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlChildRole
import com.intellij.util.ProcessingContext
import com.jetbrains.php.completion.PhpLookupElement
import com.jetbrains.php.lang.psi.PhpFile

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
                    val range = nameElement.textRange.shiftLeft(element.textRange.startOffset)

                    val project = element.project

                    val result = mutableListOf<PsiReference>()

                    FilenameIndex.processFilesByName(
                        element.name + TempestFrameworkUtil.TEMPLATE_PREFIX,
                        true,
                        GlobalSearchScope.projectScope(project),
                        {
                            val psiFile = it.findPsiFile(project) ?: return@processFilesByName true
//                            println("found file $it for ${element.name}, range ${range}")
                            result.add(PsiReferenceBase.createSelfReference(element, range, psiFile))

                            true
                        })

                    return result.toTypedArray()
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
                    val result = mutableListOf<PsiReference>()
                    val attribute = element as? XmlAttribute ?: return emptyArray()
                    val htmlTag = attribute.parent as? HtmlTag ?: return emptyArray()

                    result.add(TempestAttributeReference(element, htmlTag))
                    return result.toTypedArray()
//                        .apply { println("found references for ${element} ${this.joinToString { it.toString() }}") }
                }
            },
            HIGHER_PRIORITY
        )
    }
}

class TempestAttributeReference(element: XmlAttribute, private val htmlTag: HtmlTag) :
    PsiPolyVariantReferenceBase<PsiElement>(element, element.nameElement.textRangeInParent, false) {
    override fun getVariants(): Array<out Any> {
        val fileReferences = htmlTag.references.filter { it is Immediate<*> }

        val lookupPrefix = element.text.commonPrefixWith("::")

        return fileReferences
            .mapNotNull { it.resolve() as? PhpFile }
            .flatMap { it.getPhpViewVariables() }
            .flatMap {
                listOf(
                    object : PhpLookupElement(it) {
                        override fun getLookupString() = lookupPrefix + lookupString
                        override fun getAllLookupStrings() = setOf(lookupPrefix + lookupString)
                        override fun handleInsert(context: InsertionContext) {
                            XmlAttributeInsertHandler.INSTANCE.handleInsert(context, this)
                        }
                    },
                )
            }
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult?> {
//        println("resolve ${element.text} for ${htmlTag.name}")
//        println("range ${element.textRangeInParent} ${element.textRange}")

        return emptyArray()
    }
}
