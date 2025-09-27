package com.github.tempest.framework.console.run

import com.github.tempest.framework.TempestBundle
import com.github.tempest.framework.TempestIcons
import com.github.tempest.framework.console.index.ConsoleCommandsIndex
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.runAnything.activity.RunAnythingAnActionProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ReadAction
import com.intellij.util.indexing.FileBasedIndex

class TempestRunAnythingProvider : RunAnythingAnActionProvider<TempestRunCommandAction>() {
    override fun getCommand(value: TempestRunCommandAction) =
        TempestBundle.message("action.run.target.command", value.commandName)

    override fun getHelpCommandPlaceholder() = "tempest <command>"

    override fun getCompletionGroupTitle() = "Tempest"

    override fun getHelpCommand() = "tempest"

    override fun getHelpGroupTitle() = "PHP"

    override fun getHelpIcon() = TempestIcons.TEMPEST

    override fun getIcon(value: TempestRunCommandAction) = AllIcons.Actions.Execute

    override fun getValues(dataContext: DataContext, pattern: String): Collection<TempestRunCommandAction> {
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return emptyList()

        return ReadAction.compute<Collection<TempestRunCommandAction>, Throwable> {
            FileBasedIndex.getInstance()
                .getAllKeys(ConsoleCommandsIndex.key, project)
                .map { TempestRunCommandAction(it) }
        }
    }
}