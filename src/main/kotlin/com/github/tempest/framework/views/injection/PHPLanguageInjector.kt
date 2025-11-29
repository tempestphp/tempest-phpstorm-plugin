package com.github.tempest.framework.views.injection

import com.github.tempest.framework.TempestFrameworkUtil
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.tree.util.children
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.html.HtmlRawTextImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlToken
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment

class PHPLanguageInjector : MultiHostInjector {
    private val templateTags = mapOf(
        "{!!" to "!!}",
        "{{" to "}}",
    )

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, element: PsiElement) {
        when (element) {
            is XmlAttributeValue -> injectIntoAttribute(element, registrar)
            is HtmlTag -> injectIntoHtmlTag(element, registrar)
            is XmlText -> (element as? PsiLanguageInjectionHost)?.let { injectIntoText(it, registrar) }
        }
    }

    private fun injectIntoAttribute(element: XmlAttributeValue, registrar: MultiHostRegistrar) {
        val attribute = element.parent as? XmlAttribute ?: return
        if (!attribute.name.startsWith(':')) return

        val host = element as? PsiLanguageInjectionHost ?: return

        registrar
            .startInjecting(PhpLanguage.INSTANCE)
            .addPlace(getVarDeclarationsPrefix(element), "?>", host, TextRange(0, host.textLength))
            .doneInjecting()
    }

    private fun injectIntoHtmlTag(element: HtmlTag, registrar: MultiHostRegistrar) {
        element.children
            .filterIsInstance<HtmlRawTextImpl>()
            .forEach { injectIntoText(HtmlTextInjectionHostWrapper(it), registrar) }
    }

    private fun injectIntoText(element: PsiLanguageInjectionHost, registrar: MultiHostRegistrar) {
        val tokens = element.node.children()
            .filter { it is XmlToken }
            .toList()
            .takeIf { it.size >= 2 } ?: return

        val openTag = tokens.find { it.text in templateTags }?.psi ?: return
        val closeTag = tokens.find { it.text == templateTags[openTag.text] }?.psi ?: return
        val range = TextRange(openTag.textRangeInParent.endOffset, closeTag.startOffsetInParent)

        registrar
            .startInjecting(PhpLanguage.INSTANCE)
            .addPlace(getVarDeclarationsPrefix(element), "?>", element, range)
            .doneInjecting()
    }

    override fun elementsToInjectIn() = listOf(
        XmlAttributeValue::class.java,
        XmlText::class.java,
        HtmlTag::class.java,
    )

    private fun getVarDeclarationsPrefix(element: PsiElement): String {
        val file = element.containingFile ?: return DEFAULT_PREFIX
        if (!file.name.endsWith(TempestFrameworkUtil.TEMPLATE_SUFFIX)) return DEFAULT_PREFIX

        val searchFile = file.viewProvider.getPsi(PhpLanguage.INSTANCE) ?: file
        val varDeclarations = PsiTreeUtil.findChildrenOfType(searchFile, PhpDocComment::class.java)
            .filter { "@var" in it.text }
            .joinToString(" ") { it.text }

        return if (varDeclarations.isEmpty()) DEFAULT_PREFIX else "<?php $varDeclarations ?>$DEFAULT_PREFIX"
    }

    companion object {
        private const val DEFAULT_PREFIX = "<?="
    }
}