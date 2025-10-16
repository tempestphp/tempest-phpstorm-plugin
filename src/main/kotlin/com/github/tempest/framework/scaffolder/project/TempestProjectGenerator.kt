package com.github.tempest.framework.scaffolder.project

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.ide.util.projectWizard.WebProjectTemplate
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.php.composer.ComposerProjectGenerator
import com.jetbrains.php.composer.ComposerProjectSettings
import com.jetbrains.php.composer.addDependency.ComposerPackage
import com.jetbrains.php.composer.execution.ComposerExecution
import com.jetbrains.php.composer.execution.executable.ExecutableComposerExecution
import com.jetbrains.php.composer.execution.phar.PharComposerExecution
import com.jetbrains.php.config.interpreters.PhpInterpretersManagerImpl

class TempestProjectGenerator : WebProjectTemplate<TempestProjectGeneratorSettings>() {
    override fun getName() = "Tempest"

    override fun generateProject(
        project: Project,
        baseDir: VirtualFile,
        settings: TempestProjectGeneratorSettings,
        module: Module
    ) {
        val isDownload = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("composer") == null
        val composerExecutable = when {
            isDownload -> {
                val phpInterpretersManager = PhpInterpretersManagerImpl.getInstance(project)
                PharComposerExecution(
                    phpInterpretersManager.interpreters.firstOrNull()?.id,
                    "",
                    false,
                )
            }

            else -> ExecutableComposerExecution("composer")
        }

        println("generateProject: $baseDir, $settings, $module")
        val version = if (settings.version != "latest") settings.version else null

        val composerSettings = ComposerProjectSettings(
            isDownload,
            ComposerPackage(settings.template),
            version,
            "--no-progress --no-interaction --ansi --remove-vcs",
            composerExecutable as ComposerExecution,
            null,
            settings.createGit,
        )
        ComposerProjectGenerator().generateProject(project, baseDir, composerSettings, module)

        println("generateProject: $baseDir, $settings, $module")
    }

    override fun createPeer() = TempestProjectPeer()

    override fun getDescription() = "Tempest project template"
}