package com.github.tempest.framework.router.index

import com.github.tempest.framework.TempestFrameworkClasses
import com.github.tempest.framework.TempestFrameworkUtil
import com.github.tempest.framework.common.index.AbstractIndex
import com.github.tempest.framework.common.index.ObjectStreamDataExternalizer
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpAttribute

private typealias RoutesListIndexType = Route

/**
 * Stores uri -> methods association
 */
class RoutesListIndex : AbstractIndex<RoutesListIndexType>() {
    companion object {
        val key = ID.create<String, RoutesListIndexType>("Tempest.Routes.List")
    }

    override fun getVersion() = 2

    override fun getName() = key

    override fun getValueExternalizer() = ObjectStreamDataExternalizer<RoutesListIndexType>()

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.fileType == PhpFileType.INSTANCE &&
                !it.name.endsWith(TempestFrameworkUtil.TEMPLATE_SUFFIX)
    }

    override fun getIndexer() = DataIndexer<String, RoutesListIndexType, FileContent> { inputData ->
        inputData
            .psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
            .filter { it.fqn in TempestFrameworkClasses.ROUTES }
            .mapNotNull { RouterIndexUtils.createRouteFromAttribute(it) }
            .associateBy { it.pattern }
    }
}