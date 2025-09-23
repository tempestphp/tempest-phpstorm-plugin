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
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpNamedElement

private typealias RouterActionsIndexType = String

/**
 * Stores uri -> controller/action association
 */
class RouterActionsIndex : AbstractIndex<RouterActionsIndexType>() {
    companion object {
        val key = ID.create<String, RouterActionsIndexType>("Tempest.Routes.Actions")
    }

    override fun getVersion() = 1

    override fun getName() = key

    override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.fileType == PhpFileType.INSTANCE &&
                !it.name.endsWith(TempestFrameworkUtil.TEMPLATE_SUFFIX)
    }

    override fun getIndexer() = DataIndexer<String, RouterActionsIndexType, FileContent> { inputData ->
        inputData
            .psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
            .filter { it.fqn in TempestFrameworkClasses.ROUTES }
            .mapNotNull { attribute ->
                attribute.owner to attribute.arguments
                    .firstOrNull { it.name == "uri" || it.name.isEmpty() }
                    ?.argument
                    ?.value
                    ?.let { StringUtil.unquoteString(it) }
            }
            .filter { it.first is Method }
            .filter { !it.second.isNullOrEmpty() }
            .associate { it.second to (it.first as PhpNamedElement).fqn }
//            .apply { println("file: ${inputData.file}, result: $this") }
    }
}