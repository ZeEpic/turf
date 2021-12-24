package me.zeepic.turf.commands

import me.zeepic.turf.models.*
import me.zeepic.turf.util.fill
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector


class ResetCommand(name: String) : CustomCommand(name) {

    override fun executeCommand(player: Player, args: Array<out String>): Boolean {

        if (args.size > 4) return false
        Game.clearPlayers()
        Game.resetTimer()
        // take only the numbers from the args, and then use that to change the size of the game map
        Game.changeMap(MapSize(args.mapNotNull { it.toIntOrNull() }), player.world)
        val world = Game.world
        world.entities
            .filter { it.type == EntityType.DROPPED_ITEM }
            .forEach { it.remove() }
        Material.AIR.fill(world,
            Vector(0, platformHeight, 0),
            Vector(width * 6, platformHeight + maxHeight, width * 6))
        Material.WHITE_CONCRETE.fill(world,
            Vector(width * 2, platformHeight, width * 2),
            Vector(width * 4, platformHeight, width * 4))
        Team.values().forEach {
            it.score = 21
            for (i in 0 until width * 2) it.fillAt(world, i >= it.score, i)
        }
        Bukkit.getOnlinePlayers().forEach {
            val team = Game add it
            it.spawn(team)
            it.sendMessage(team.toString())
        }
        Game.state = GameState.STARTING
        Game.scheduleTimer()

        return true
    }

}