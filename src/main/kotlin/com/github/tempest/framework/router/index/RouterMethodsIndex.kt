package com.github.tempest.framework.router.index

import com.github.tempest.framework.TempestFrameworkClasses
import com.github.tempest.framework.TempestFrameworkUtil
import com.github.tempest.framework.common.index.AbstractIndex
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpAttribute

private typealias RouterMethodsIndexType = String

/**
 * Stores uri -> methods association
 */
class RouterMethodsIndex : AbstractIndex<RouterMethodsIndexType>() {
    companion object {
        val key = ID.create<String, RouterMethodsIndexType>("Tempest.Routes.Methods")
    }

    override fun getVersion() = 3

    override fun getName() = key

    override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.fileType == PhpFileType.INSTANCE &&
                !it.name.endsWith(TempestFrameworkUtil.TEMPLATE_SUFFIX)
    }

    override fun getIndexer() = DataIndexer<String, RouterMethodsIndexType, FileContent> { inputData ->
        inputData
            .psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
            .filter { it.fqn in TempestFrameworkClasses.ROUTES }
            .mapNotNull { attribute ->
                attribute.name to attribute.arguments
                    .firstOrNull { it.name == "uri" || it.name.isEmpty() }
                    ?.argument
                    ?.value
                    ?.let { StringUtil.unquoteString(it) }
            }
            .filter { !it.first.isNullOrEmpty() && !it.second.isNullOrEmpty() }
            .associate { it.second to it.first!!.lowercase() }
//            .apply { println("file: ${inputData.file}, result: $this") }
    }
}