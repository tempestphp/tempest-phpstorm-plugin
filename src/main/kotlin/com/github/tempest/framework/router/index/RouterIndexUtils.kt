package com.github.tempest.framework.router.index

import com.github.tempest.framework.router.StringUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpAttribute

object RouterIndexUtils {
    fun getAllRoutes(project: Project): Collection<Route> {
        val fileBasedIndex = FileBasedIndex.getInstance()

        return fileBasedIndex
            .getAllKeys(RoutesListIndex.key, project)
            .flatMap { fileBasedIndex.getValues(RoutesListIndex.key, it, GlobalSearchScope.allScope(project)) }
            .filterNotNull()
    }

    fun getRoutesByPattern(pattern: String, project: Project): Collection<Route> {
        val fileBasedIndex = FileBasedIndex.getInstance()

        return fileBasedIndex
            .getValues(RoutesListIndex.key, pattern, GlobalSearchScope.allScope(project))
            .filterNotNull()
    }

    fun createRouteFromAttribute(attribute: PhpAttribute): Route? {
        val httpMethod = attribute.name?.uppercase() ?: return null
        val method = attribute.owner as? Method ?: return null
        val pattern = attribute.arguments
            .firstOrNull { it.name == "uri" || it.name.isEmpty() }
            ?.argument
            ?.value
            ?.let { StringUtil.unquoteString(it) }
            ?: return null
        val parameters = StringUtils
            .findRouterParameters(pattern)
            .map {
                RouteParameter(
                    it.groupValues[1],
                    it.groupValues.getOrNull(2) ?: "",
                )
            }
            .toSet()

        return Route(
            pattern = pattern,
            action = method.fqn,
            method = httpMethod,
            parameters = parameters,
        )
    }
}