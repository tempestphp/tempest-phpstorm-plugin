package com.github.tempest.framework.router

object StringUtils {
    fun findRouterParameters(text: String): List<MatchResult> =
        Regex("\\{([^:\\/}]+)(?::([^}]+))?(?:[}\\/]|$)")
            .findAll(text)
            .toList()
}