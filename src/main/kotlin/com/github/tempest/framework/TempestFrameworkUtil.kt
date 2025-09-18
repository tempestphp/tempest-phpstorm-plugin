package com.github.tempest.framework

object TempestFrameworkUtil {
    const val TEMPLATE_PREFIX = ".view.php"

    private val BUILT_IN_COMPONENTS = setOf(
        "x-base",
        "x-form",
        "x-input",
        "x-submit",
        "x-csrf-token",
        "x-icon",
        "x-vite-tags",
        "x-template",
        "x-slot",
        "x-markdown",
        "x-component",
    )

    fun isBuiltInComponent(name: String): Boolean = BUILT_IN_COMPONENTS.contains(name)
}
