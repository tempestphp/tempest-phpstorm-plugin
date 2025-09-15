package com.github.tempest.framework.views

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.tree.util.children
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlToken
import com.jetbrains.php.lang.PhpLanguage

class PHPLanguageInjector : MultiHostInjector {
    override fun getLanguagesToInject(
        registrar: MultiHostRegistrar,
        element: PsiElement
    ) {
        when (element) {
            is XmlAttributeValue -> {
                val attribute = element.parent as? XmlAttribute ?: return

                if (!attribute.name.startsWith(':') || attribute.name.startsWith("::")) return

                val injectableHost = element as? PsiLanguageInjectionHost ?: return

//                println("injecting PHP into ${injectableHost.text}")
//        registrar
//            .startInjecting(Language.findLanguageByID("PHP") ?: return)
//            .addPlace("<?php ","", injectableHost, TextRange(0, injectableHost.textLength))
//            .doneInjecting()
                registrar
                    .startInjecting(Language.findLanguageByID("PHP") ?: return)
                    .addPlace("<?=", "?>", injectableHost, TextRange(0, injectableHost.textLength))
                    .doneInjecting()
            }

            is XmlText -> {
                val injectableHost = element as? PsiLanguageInjectionHost ?: return
                val children = element.children.filter { it is XmlToken }
                val openTag = children.firstOrNull() ?: return
                val closeTag = children.lastOrNull()

//                println("openTag: ${openTag.text}, closeTag: ${closeTag?.text}")
                if ((openTag.text == "{!!" && closeTag?.text == "!!}") || (openTag.text == "{{" && closeTag?.text == "}}")) {
//                    println("injecting")
                    registrar.startInjecting(Language.findLanguageByID("InjectablePHP") ?: return)
                        .addPlace(
                            "<?=", "?>", injectableHost,
                            TextRange(
                                openTag.textRangeInParent.endOffset,
                                closeTag?.startOffsetInParent ?: injectableHost.textLength
                            )
                        )
                        .doneInjecting()
                }
            }
        }
    }

    override fun elementsToInjectIn() = listOf(
        XmlAttributeValue::class.java,
        XmlText::class.java,
    )
}