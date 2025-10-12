package com.github.tempest.framework

object TempestFrameworkClasses {
    const val ConsoleCommand = "\\Tempest\\Console\\ConsoleCommand"
    const val CONTAINER = "\\Tempest\\Container\\Container"
    const val CONTAINER_INVOKE = "\\Tempest\\Container\\Container::invoke"

    const val FUNCTION_URI = "\\Tempest\\Router\\uri"

    const val ROUTER_GET = "\\Tempest\\Router\\Get"
    const val ROUTER_POST = "\\Tempest\\Router\\Post"
    const val ROUTER_PUT = "\\Tempest\\Router\\Put"
    const val ROUTER_PATCH = "\\Tempest\\Router\\Patch"
    const val ROUTER_DELETE = "\\Tempest\\Router\\Delete"
    const val ROUTER_OPTIONS = "\\Tempest\\Router\\Options"
    const val ROUTER_HEAD = "\\Tempest\\Router\\Head"
    const val ROUTER_CONNECT = "\\Tempest\\Router\\Connect"
    const val ROUTER_TRACE = "\\Tempest\\Router\\Trace"

    val ROUTES = listOf(
        ROUTER_GET,
        ROUTER_POST,
        ROUTER_PUT,
        ROUTER_PATCH,
        ROUTER_DELETE,
        ROUTER_OPTIONS,
        ROUTER_HEAD,
        ROUTER_CONNECT,
        ROUTER_TRACE,
    )
}