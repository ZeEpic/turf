package me.zeepic.turf.models

import me.zeepic.turf.util.atY
import me.zeepic.turf.util.fillCentered
import me.zeepic.turf.util.toInt
import me.zeepic.turf.util.toTitle
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.random.Random.Default.nextInt


enum class Team (
    val groundType: Material,
    val blockType: Material,
    private val spawnLocation: Vector,
    private var score: Int = 0) {

    BLUE(
        Material.BLUE_CONCRETE, Material.BLUE_WOOL,
        Vector(width * 3, platformHeight, startFromEnd)
    ),
    RED(
        Material.RED_CONCRETE, Material.RED_WOOL,
        Vector(startFromEnd, platformHeight, width * 3)
    ),
    GREEN(
        Material.GREEN_CONCRETE, Material.GREEN_WOOL,
        Vector((width * 6) - startFromEnd, platformHeight, width * 3)
    ),
    YELLOW(
        Material.YELLOW_CONCRETE, Material.YELLOW_WOOL,
        Vector(width * 3, platformHeight, (width * 6) - startFromEnd)
    );

    companion object Random {
        fun random(): Team {
            return values()[nextInt(values().size)]
        }
    }

    fun fillAt(world: World, clear: Boolean = false) {
        val onZ = (spawnLocation.x == (width * 3.0))
        val offsetVector = Vector((score - startFromEnd) * (!onZ).toInt(), 0, (score - startFromEnd) * onZ.toInt())
        val center = when(this) {
            GREEN, YELLOW -> spawnLocation.clone().subtract(offsetVector)
            else -> spawnLocation.clone().add(offsetVector)
        }
        val material = if (clear) Material.WHITE_CONCRETE else groundType
        val size = Vector(width * onZ.toInt(), maxHeight, width * (!onZ).toInt())
        Material.AIR.fillCentered(world, center, size)
        material.fillCentered(world, center, size.atY(0.0))
    }

    fun teleportToSpawn(player: Player) =
        player.teleport(spawnLocation.toLocation(player.world).toHighestLocation())

    fun addScore() {
        score += 1
        fillAt(Game.world, false)
        if (score > maxScore) Game.declareWinner(this)
    }

    fun removeScore() {
        score -= 1
        fillAt(Game.world, true)
        if (score > 0) return
        remove()
        val remainingTeams = Game.remainingTeams()
        if (remainingTeams.size == 1) {
            Game.declareWinner(remainingTeams.first())
        }
    }

    private fun remove() {
        Bukkit.broadcast(Component.text("${name.toTitle()} team is out of the game."))
        Game.teamPlayers(this).forEach {
            val player = it.key
            Game remove player
            Game reset player
            player.showTitle(Title.title(Component.text("Game Over"), Component.text("Your team ran out of space!")))
        }
    }

}
