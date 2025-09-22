package com.github.tempest.framework.router

import com.github.tempest.framework.TempestFrameworkClasses
import com.github.tempest.framework.TempestFrameworkUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpAttribute

private typealias RouteType = String

class RoutesIndex : AbstractIndex<RouteType>() {
    companion object {
        val key = ID.create<String, RouteType>("Tempest.Routes")
    }

    override fun getVersion() = 1

    override fun getName() = key

    override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.fileType == PhpFileType.INSTANCE &&
                !it.name.endsWith(TempestFrameworkUtil.TEMPLATE_SUFFIX)
    }

    override fun getIndexer() = DataIndexer<String, RouteType, FileContent> { inputData ->
        inputData
            .psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
            .filter { it.fqn == TempestFrameworkClasses.ConsoleCommand }
            .mapNotNull { attribute ->
                attribute.arguments
                    .firstOrNull { it.name == "name" || it.name.isEmpty() }
                    ?.argument
                    ?.value
            }
            .map { StringUtil.unquoteString(it) }
            .associateBy { it }
//            .apply { println("file: ${inputData.file}, result: $this") }
    }
}