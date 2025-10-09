package com.github.tempest.framework.common.utils

object StringUtils {
    fun findTextBetweenParenthesis(text: String): List<MatchResult> =
        Regex("\\{([^}/]+)(?:[}/]|$)")
            .findAll(text)
            .toList()
}