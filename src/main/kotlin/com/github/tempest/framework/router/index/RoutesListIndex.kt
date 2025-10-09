package com.github.tempest.framework.router.index

import com.github.tempest.framework.TempestFrameworkClasses
import com.github.tempest.framework.TempestFrameworkUtil
import com.github.tempest.framework.common.index.AbstractIndex
import com.github.tempest.framework.common.index.ObjectStreamDataExternalizer
import com.github.tempest.framework.router.StringUtils
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.Method
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
            .mapNotNull { attribute ->
                val httpMethod = attribute.name?.uppercase() ?: return@mapNotNull null
                val method = attribute.owner as? Method ?: return@mapNotNull null
                val pattern = attribute.arguments
                    .firstOrNull { it.name == "uri" || it.name.isEmpty() }
                    ?.argument
                    ?.value
                    ?.let { StringUtil.unquoteString(it) }
                    ?: return@mapNotNull null
                val parameters = StringUtils
                    .findRouterParameters(pattern)
                    .map {
                        RouteParameter(
                            it.groupValues[1],
                            it.groupValues.getOrNull(2) ?: "",
                        )
                    }
                    .toSet()

                Pair(
                    pattern,
                    Route(
                        pattern = pattern,
                        action = method.fqn,
                        method = httpMethod,
                        parameters = parameters,
                    ),
                )
            }
            .associate { it.first to it.second }
    }
}