package me.zeepic.turf

import me.zeepic.turf.commands.CustomCommand
import me.zeepic.turf.commands.ResetCommand
import me.zeepic.turf.listeners.PlayerListener
import me.zeepic.turf.listeners.WorldListener
import me.zeepic.turf.models.Game
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    companion object {
        lateinit var instance: JavaPlugin
    }

    private fun registerListeners(vararg listeners: Listener) {
        val manager = server.pluginManager
        listeners.forEach { manager.registerEvents(it, this) }
    }

    private fun registerCommands(vararg commands: CustomCommand) {
        commands.forEach { getCommand(it.name)?.setExecutor(it) }
    }

    override fun onEnable() {
        // Plugin startup logic
        instance = this
        registerListeners(
            WorldListener(),
            PlayerListener()
        )
        registerCommands(
            ResetCommand("reset")
        )

    }

    override fun onDisable() {
        // Plugin shutdown logic
        server.scheduler.cancelTask(Game.timerID())
    }

}