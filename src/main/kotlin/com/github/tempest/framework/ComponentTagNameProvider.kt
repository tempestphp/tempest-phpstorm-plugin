package com.github.tempest.framework

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlTagNameProvider

class ComponentTagNameProvider : XmlTagNameProvider {
    override fun addTagNameVariants(
        elements: MutableList<LookupElement>, tag: XmlTag, prefix: String?
    ) {
        val project = tag.project

        val result = mutableListOf<String>()
        FilenameIndex.processAllFileNames(
            {
                if (it.startsWith("x-") && it.endsWith(TempestFrameworkUtil.TEMPLATE_PREFIX)) {
                    val name = it.removeSuffix(TempestFrameworkUtil.TEMPLATE_PREFIX)
                    if (!TempestFrameworkUtil.isBuiltInComponent(name)) {
                        result.add(name)
                    }
                }

                true
            },
            GlobalSearchScope.projectScope(project),
            null,
        )

        result.distinct().map {
            LookupElementBuilder.create(it).withIcon(TempestIcons.TEMPEST).withTypeText("Tempest Component")
        }.apply { elements.addAll(this) }
    }
}
