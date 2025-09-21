package com.github.tempest.framework.views.references

import com.github.tempest.framework.php.getPhpViewVariables
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.XmlAttributeInsertHandler
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.xml.XmlAttribute
import com.jetbrains.php.completion.PhpLookupElement
import com.jetbrains.php.lang.psi.PhpFile

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