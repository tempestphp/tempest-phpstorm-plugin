package com.github.tempest.framework.scaffolder.project

import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.platform.GeneratorPeerImpl
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.util.concurrency.AppExecutorUtil
import com.jetbrains.php.composer.addDependency.ComposerPackage
import com.jetbrains.php.composer.json.PhpComposerJsonCompletionContributor.PhpComposerPackageVersionsProvider
import java.util.concurrent.Callable

class TempestProjectPeer() : GeneratorPeerImpl<TempestProjectGeneratorSettings>() {
    var versions = MutableCollectionComboBoxModel(mutableListOf("latest"))
    val mySettings = TempestProjectGeneratorSettings()

    val myPanel = panel {
        row("Template") {
            comboBox(listOf("tempest/app"))
                .bindItem(mySettings::template.toNullableProperty())
        }
        row("Version") {
            comboBox(versions)
                .bindItem(mySettings::version.toNullableProperty())
        }
        row("Create Git") {
            checkBox("")
                .bindSelected(mySettings::createGit)
        }
    }

    init {
        ApplicationManager.getApplication().invokeLater {
            ReadAction.nonBlocking(Callable {
                PhpComposerPackageVersionsProvider
                    .fetchPackageVersions("tempest/app")
                    .sortedWith(ComposerPackage.VERSIONS_COMPARATOR)
                    .apply { versions.add(this) }
            })
                .finishOnUiThread(ModalityState.defaultModalityState()) {}
                .submit(AppExecutorUtil.getAppExecutorService())
        }
    }

    override fun getComponent(myLocationField: TextFieldWithBrowseButton, checkValid: Runnable) = myPanel

    override fun buildUI(settingsStep: SettingsStep) {
        settingsStep.addSettingsComponent(myPanel)
    }

    override fun getSettings(): TempestProjectGeneratorSettings {
        myPanel.apply()
        return mySettings
    }

    override fun validate(): ValidationInfo? {
        myPanel.validate()
        return myPanel.validateAll().firstOrNull()
    }
}