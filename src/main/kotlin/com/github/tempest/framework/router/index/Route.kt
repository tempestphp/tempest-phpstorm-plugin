package com.github.tempest.framework.router.index

import java.io.Serializable

data class Route(
    val pattern: String,
    val action: String,
    val method: String,
    val parameters: Set<RouteParameter>,
) : Serializable

data class RouteParameter(
    val name: String,
    val pattern: String,
) : Serializable