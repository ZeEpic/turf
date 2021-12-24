package me.zeepic.turf.models

import me.zeepic.turf.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.random.Random.Default.nextInt


enum class Team (
    val groundType: Material,
    val blockType: Material,
    private val spawnLocation: Vector,
    private val rotation: Int,
    var score: Int = startFromEnd * 2 + 1) {

    BLUE(
        Material.BLUE_CONCRETE, Material.BLUE_WOOL,
        Vector(width * 3, platformHeight, startFromEnd),
        0
    ),
    RED(
        Material.RED_CONCRETE, Material.RED_WOOL,
        Vector(startFromEnd, platformHeight, width * 3),
        -90
    ),
    GREEN(
        Material.GREEN_CONCRETE, Material.GREEN_WOOL,
        Vector((width * 6) - startFromEnd, platformHeight, width * 3),
        90
    ),
    YELLOW(
        Material.YELLOW_CONCRETE, Material.YELLOW_WOOL,
        Vector(width * 3, platformHeight, (width * 6) - startFromEnd),
        -180
    );

    companion object Random {
        fun random(): Team {
            return values()[nextInt(values().size)]
        }
    }

    fun fillAt(world: World, clear: Boolean = false, distance: Int = score) {
        val onZ = (spawnLocation.x == (width * 3.0))
        val offsetVector = Vector((distance - startFromEnd) * (!onZ).toInt(), 0, (distance - startFromEnd) * onZ.toInt())
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
        player.teleport(spawnLocation
            .withRandomOffset((width / 2.0).toInt(), startFromEnd)
            .toLocation(player.world)
            .toHighestLocation()
            .rotated(0, rotation)
            .add(0.0, 1.0, 0.0))

    fun addScore() {
        fillAt(Game.world, false)
        score += 1
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

    fun getPlaceableBlock() =
        ItemStack(blockType)
            .canPlaceOn(blockType)
            .canPlaceOn(groundType)

}
