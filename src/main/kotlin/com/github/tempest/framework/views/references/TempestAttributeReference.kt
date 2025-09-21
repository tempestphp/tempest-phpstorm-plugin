package com.github.tempest.framework.views.references

import com.github.tempest.framework.php.getPhpViewVariables
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.XmlAttributeInsertHandler
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.xml.XmlAttribute
import com.intellij.webSymbols.utils.NameCaseUtils
import com.jetbrains.php.completion.PhpLookupElement
import com.jetbrains.php.lang.psi.PhpFile

class TempestAttributeReference(element: XmlAttribute, private val htmlTag: HtmlTag) :
    PsiPolyVariantReferenceBase<XmlAttribute>(element) {
    override fun getVariants(): Array<out Any> {
        val fileReferences = htmlTag.references.filter { it is Immediate<*> }

        return fileReferences
            .mapNotNull { it.resolve() as? PhpFile }
            .flatMap { it.getPhpViewVariables() }
            .flatMap {
                val attributeName = it.name.toKebabCase()
                listOf(
                    object : PhpLookupElement(it) {
                        override fun getLookupString() = attributeName
                        override fun getAllLookupStrings() = setOf(attributeName)
                        override fun handleInsert(context: InsertionContext) {
                            XmlAttributeInsertHandler.INSTANCE.handleInsert(context, this)
                        }
                    },
                )
            }
            .toTypedArray()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        val originalName = rangeInElement.substring(element.name)

        // handle camelCaseVariables, they're wrongly cased
        if (originalName.inCamelCase()) return ResolveResult.EMPTY_ARRAY

        val fileReferences = htmlTag.references.filter { it is Immediate<*> }

        val lookupVariable = originalName.toCamelCase()

//        println("lookupVariable $lookupVariable fileReferences $fileReferences")
//        println("element: ${element} (${element.text}) rangeInParent: ${rangeInElement}")

        // todo: there's a bug with attributes started with ":", VueJS webTypes filter references under such attributes
        return fileReferences
            .mapNotNull { it.resolve() as? PhpFile }
            .flatMap { it.getPhpViewVariables() }
            .filter { it.name == lookupVariable }
//            .apply { println("variants $this") }
            .run { PsiElementResolveResult.createResults(this) }
//            .apply { println("resolve $lookupVariable ${this.joinToString { it.element.toString() }}") }
    }

    override fun isSoft() = !element.name.startsWith(":")

    override fun getRangeInElement() = when {
        element.name.startsWith(":") -> element.nameElement.textRangeInParent.shiftRight(1).grown(-1)
        else -> element.nameElement.textRangeInParent
    }
}

fun String.toCamelCase() = NameCaseUtils.toCamelCase(this)
fun String.toKebabCase() = NameCaseUtils.toKebabCase(this)
fun String.inCamelCase() = StringUtil.hasUpperCaseChar(this) && this != NameCaseUtils.toKebabCase(this)