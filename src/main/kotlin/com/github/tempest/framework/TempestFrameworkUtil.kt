package com.github.tempest.framework

object TempestFrameworkUtil {
    const val TEMPLATE_SUFFIX = ".view.php"
    const val COMPONENT_NAME_PREFIX = "x-"

    val BUILT_IN_DIRECTIVE_ATTRIBUTES = setOf(
        ":if",
        ":elseif",
        ":else",
        ":foreach",
        ":forelse",
    )
}