package com.github.tempest.framework.scaffolder.project

import com.github.tempest.framework.TempestIcons
import com.github.tempest.framework.scaffolder.project.override.WebTemplateProjectWizardStep
import com.intellij.ide.util.projectWizard.WebTemplateNewProjectWizardBase
import com.intellij.ide.wizard.NewProjectWizardBaseStep
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.RootNewProjectWizardStep

class TempestGeneratorProjectWizard : WebTemplateNewProjectWizardBase() {
    override val id = "tempest-project"
    override val name = "Tempest"
    override val icon = TempestIcons.TEMPEST

    val template = TempestProjectGenerator()

    override fun createTemplateStep(parent: NewProjectWizardBaseStep): NewProjectWizardStep {
        return RootNewProjectWizardStep(parent.context)
            .nextStep { WebTemplateProjectWizardStep(parent, template) }
    }
}