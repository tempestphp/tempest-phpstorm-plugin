package com.github.tempest.framework.console.run

import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import com.jetbrains.php.run.PhpCommandLineSettings
import com.jetbrains.php.run.PhpRunConfigurationSettings

class TempestConsoleCommandRunConfigurationSettings : PhpRunConfigurationSettings, LocatableRunConfigurationOptions() {
    private val myCommandName = string("").provideDelegate(this, "commandName")
    private val myBinary = string("./tempest").provideDelegate(this, "binary")
    private val myWorkingDirectory = string("").provideDelegate(this, "binary")

    var commandName: String?
        get() = myCommandName.getValue(this)
        set(scriptName) {
            myCommandName.setValue(this, scriptName)
        }

    var binary: String?
        get() = myBinary.getValue(this)
        set(scriptName) {
            myBinary.setValue(this, scriptName)
        }

    var documentRoot: String?
        get() = myBinary.getValue(this)
        set(scriptName) {
            myBinary.setValue(this, scriptName)
        }

    var commandLineSettings = PhpCommandLineSettings()

    override fun getWorkingDirectory() = myWorkingDirectory.getValue(this)

    override fun setWorkingDirectory(p0: String?) {
        myWorkingDirectory.setValue(this, p0)
    }
}