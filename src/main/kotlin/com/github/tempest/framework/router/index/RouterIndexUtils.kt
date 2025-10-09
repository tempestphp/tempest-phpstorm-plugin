package com.github.tempest.framework.router.index

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

object RouterIndexUtils {
    fun getAllRoutes(project: Project): Collection<Route> {
        val fileBasedIndex = FileBasedIndex.getInstance()

        return fileBasedIndex
            .getAllKeys(RoutesListIndex.key, project)
            .flatMap { fileBasedIndex.getValues(RoutesListIndex.key, it, GlobalSearchScope.allScope(project)) }
            .filterNotNull()
    }

    fun getRoute(pattern: String, project: Project): Collection<Route> {
        val fileBasedIndex = FileBasedIndex.getInstance()

        return fileBasedIndex
            .getValues(RoutesListIndex.key, pattern, GlobalSearchScope.allScope(project))
            .filterNotNull()
    }
}