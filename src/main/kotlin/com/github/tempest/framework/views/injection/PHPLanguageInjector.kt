package com.github.tempest.framework.views.injection

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.tree.util.children
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.html.HtmlRawTextImpl
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlToken

class PHPLanguageInjector : MultiHostInjector {
    override fun getLanguagesToInject(
        registrar: MultiHostRegistrar, element: PsiElement
    ) {
        when (element) {
            is XmlAttributeValue -> {
                val attribute = element.parent as? XmlAttribute ?: return

                if (!attribute.name.startsWith(':') || attribute.name.startsWith("::")) return

                val injectableHost = element as? PsiLanguageInjectionHost ?: return

                registrar.startInjecting(Language.findLanguageByID("PHP") ?: return)
                    .addPlace("<?=", "?>", injectableHost, TextRange(0, injectableHost.textLength)).doneInjecting()
            }

            is HtmlTag -> {
                element.children.mapNotNull { it as? HtmlRawTextImpl }.forEach { child ->
                    injectIntoText(HtmlTextInjectionHostWrapper(child), registrar)
                }
            }

            is XmlText -> {
//                println("element: ${element.text}, ${element.javaClass.name} ${element is PsiLanguageInjectionHost}")
                val injectableHost = element as? PsiLanguageInjectionHost ?: return
                injectIntoText(injectableHost, registrar)
            }
        }
    }

    val tagsMap = mapOf(
        "{!!" to "!!}",
        "{{" to "}}",
    )

    private fun injectIntoText(
        element: PsiLanguageInjectionHost, registrar: MultiHostRegistrar
    ) {
        val children = element.node.children().toList().filter { it is XmlToken }.apply { if (size < 2) return }

//        println("children: $children")
        val openTag = children.find { it.text == "{!!" || it.text == "{{" }?.psi ?: return
        val closeTag = children.find { it.text == tagsMap[openTag.text] }?.psi

//        println("openTag: ${openTag.text}, closeTag: ${closeTag?.text}")
        if ((openTag.text == "{!!" && closeTag?.text == "!!}") || (openTag.text == "{{" && closeTag?.text == "}}")) {
            val language = Language.findLanguageByID("PHP") ?: return

            val textRange = TextRange(openTag.textRangeInParent.endOffset, closeTag.startOffsetInParent)
//            println("injecting ${language} into $element, $textRange")
            registrar.startInjecting(language).addPlace("<?=", "?>", element, textRange).doneInjecting()
        }
    }

    override fun elementsToInjectIn() = listOf(
        XmlAttributeValue::class.java,
        XmlText::class.java,
        HtmlTag::class.java,
    )
}