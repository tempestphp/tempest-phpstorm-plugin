package com.github.tempest.framework.scaffolder.project

data class TempestProjectGeneratorSettings(
    var version: String = "latest",
    var createGit: Boolean = true,
    var template: String = "tempest/app",
)