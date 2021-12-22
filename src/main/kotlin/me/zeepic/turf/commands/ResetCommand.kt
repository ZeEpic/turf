package me.zeepic.turf.commands

import me.zeepic.turf.Main
import me.zeepic.turf.models.*
import me.zeepic.turf.util.fill
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.util.Vector


class ResetCommand(name: String) : CustomCommand(name) {

    override fun executeCommand(player: Player, args: Array<out String>): Boolean {

        Game.players.clear()
        Bukkit.getScheduler().cancelTask(Game.timer.timerID)
        Game.changeMap(MapSize(args.mapNotNull { it.toIntOrNull() }), player.world)
        val world = Game.world
        Material.AIR.fill(world,
            Vector(0, platformHeight, 0),
            Vector(width * 6, platformHeight + maxHeight, width * 6))
        Material.WHITE_CONCRETE.fill(world,
            Vector(width * 2, platformHeight, width * 2),
            Vector(width * 4, platformHeight, width * 4))
        val startingScore = startFromEnd * 2 + 1
        Game.Team.values().forEach {
            for (i in 0..(width * 2)) it.fillAt(world, true)
            for (i in 0..startingScore) it.addScore()
        }
        Bukkit.getOnlinePlayers().forEach {
            val team = Game.Team.random()
            Game.addPlayer(it, team)
            it.spawn(team)
            it.sendMessage(team.toString())
        }
        Game.giveBlocks()
        Game.state = GameState.STARTING
        Game.timer.timerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.instance, Game.timer, 0, 20L)
        return true
    }

}