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
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl
import com.intellij.psi.impl.source.xml.XmlTextImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlToken
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment

class PHPLanguageInjector : MultiHostInjector {
    companion object {
        private val templateTags = mapOf(
            "{!!" to "!!}",
            "{{" to "}}",
        )
    }

    override fun elementsToInjectIn() = listOf(
        XmlAttributeValueImpl::class.java,
        XmlTextImpl::class.java,
        HtmlTag::class.java,
    )

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, element: PsiElement) {
        val file = element.containingFile ?: return
        if (!file.name.endsWith(TempestFrameworkUtil.TEMPLATE_SUFFIX)) return

        when (element) {
            is XmlAttributeValueImpl -> injectIntoAttribute(element, registrar)
            is XmlTextImpl -> injectIntoText(element, registrar)
            is HtmlTag -> injectIntoHtmlTag(element, registrar)
        }
    }

    private fun injectIntoAttribute(element: XmlAttributeValueImpl, registrar: MultiHostRegistrar) {
        val attribute = element.parent as? XmlAttribute ?: return
        if (!attribute.name.startsWith(':')) return

        val variableDeclarations = collectVariableDeclarations(element)
        registrar
            .startInjecting(PhpLanguage.INSTANCE)
            .addPlace("$variableDeclarations<?=", "?>", element, element.textRange.shiftLeft(element.startOffset))
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

        val variableDeclarations = collectVariableDeclarations(element)

        registrar
            .startInjecting(PhpLanguage.INSTANCE)
            .addPlace("$variableDeclarations<?=", "?>", element, range)
            .doneInjecting()
    }

    private fun collectVariableDeclarations(element: PsiElement): String? {
        val file = element.containingFile ?: return null

        val searchFile = file.viewProvider.getPsi(PhpLanguage.INSTANCE) ?: file
        val variableDeclarations = PsiTreeUtil.findChildrenOfType(searchFile, PhpDocComment::class.java)
            .filter { "@var" in it.text }
            .joinToString("\n") { it.text }
            .ifEmpty { return null }

        return "<?php $variableDeclarations ?>"
    }
}