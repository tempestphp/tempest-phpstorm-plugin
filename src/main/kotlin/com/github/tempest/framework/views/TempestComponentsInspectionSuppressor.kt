package com.github.tempest.framework.views

import com.github.tempest.framework.TempestFrameworkUtil
import com.intellij.codeInspection.XmlInspectionSuppressor
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlTokenType

class TempestComponentsInspectionSuppressor : XmlInspectionSuppressor() {
    override fun isSuppressedFor(element: PsiElement, toolId: String) = when {
        element.elementType == XmlTokenType.XML_NAME -> isTagSuppressed(element.text, toolId)
        element is HtmlTag -> isTagSuppressed(element.name, toolId)
        else -> false
    }

    private fun isTagSuppressed(tagName: String, toolId: String): Boolean {
        return tagName.startsWith(TempestFrameworkUtil.COMPONENT_NAME_PREFIX)
                && toolId in listOf("HtmlUnknownTag", "CheckEmptyScriptTag")
    }
}