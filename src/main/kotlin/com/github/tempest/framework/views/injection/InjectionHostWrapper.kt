package com.github.tempest.framework.views.injection

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTFactory
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.html.HtmlRawTextImpl
import com.intellij.psi.impl.source.xml.XmlTextImpl

abstract class InjectionHostWrapper(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost {
    override fun isValidHost(): Boolean = true

    override fun updateText(text: String): PsiLanguageInjectionHost {
        this.node.treeParent.replaceChild(this.node, ASTFactory.leaf(this.node.elementType, text))
        return this
    }
}

class XmlTextInjectionHostWrapper(val myElement: XmlTextImpl) : InjectionHostWrapper(myElement.node) {
    override fun createLiteralTextEscaper() = myElement.createLiteralTextEscaper()
}

class HtmlTextInjectionHostWrapper(val myElement: HtmlRawTextImpl) : InjectionHostWrapper(myElement.node) {
    override fun createLiteralTextEscaper() = LiteralTextEscaper.createSimple(this, false)
}