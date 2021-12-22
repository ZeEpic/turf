package me.zeepic.turf.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class CustomCommand(val name: String) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        return executeCommand(sender, args)
    }

    abstract fun executeCommand(player: Player, args: Array<out String>): Boolean

}