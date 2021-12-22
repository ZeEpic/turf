package me.zeepic.turf.models

import me.zeepic.turf.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.random.Random.Default.nextInt

var maxHeight = 80
var width = 25
var platformHeight = 64
var startFromEnd = 10
const val maxScore = 150

fun Player.spawn(team: Game.Team) {
    team.teleportToSpawn(this)
    health = 20.0
    inventory.clear()
    inventory.setItem(0, ItemStack(Material.BOW)
        .withEnchantment(Enchantment.ARROW_INFINITE, 1)
        .unbreakable())
    inventory.setItem(1, ItemStack(Material.SHEARS)
        .unbreakable()
        .canBreak(team.blockType))
    inventory.setItem(9, ItemStack(Material.ARROW))
    gameMode = GameMode.ADVENTURE
}

data class MapSize(
    var _maxHeight: Int = 80,
    var _width: Int = 25,
    var _platformHeight: Int = 64,
    var _startFromEnd: Int = 10) {

    constructor(args: List<Int>) : this() {
        if (args.isNotEmpty()) _maxHeight = args[0]
        if (args.size > 1) _width = args[1]
        if (args.size > 2) _platformHeight = args[2]
        if (args.size > 3) _startFromEnd = args[3]
    }

}

object Game {

    fun changeMap(size: MapSize, world: World) {
        this.world = world
        maxHeight = size._maxHeight
        width = size._width
        platformHeight = size._platformHeight
        startFromEnd = size._startFromEnd
    }

    var state = GameState.IDLE
    val players = mutableMapOf<Player, GamePlayer>()
    val timer = Timer()
    lateinit var world: World

    infix fun team(player: Player): Team? {
        return players[player]?.team
    }

    fun addPlayer(player: Player, team: Team) {
        players[player] = GamePlayer(player, team, 0, 0)
    }

    enum class Team(
        val groundType: Material,
        val blockType: Material,
        private val spawnLocation: Vector,
        private var score: Int = 0
    ) {

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

        companion object {
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
            Material.AIR.fillCentered(world, center, Vector(25 * onZ.toInt(), maxHeight, 25 * (!onZ).toInt()))
            material.fillCentered(world, center, Vector(25 * onZ.toInt(), 0, 25 * (!onZ).toInt()))
        }

        fun teleportToSpawn(player: Player) {
            player.teleport(spawnLocation.toLocation(player.world).toHighestLocation())
        }

        fun addScore() {
            score += 1
            fillAt(world, false)
            if (score > maxScore) declareWinner(this)
        }

        fun removeScore() {
            score -= 1
            fillAt(world, true)
            if (score > 0) return
            removeTeam()
            val remainingTeams = players
                .map { it.value.team }
                .toSet()
            if (remainingTeams.size == 1) {
                declareWinner(remainingTeams.first())
            }
        }

        private fun removeTeam() {
            Bukkit.broadcast(Component.text("${name.toTitle()} team is out of the game."))
            players.filterValues { it.team == this }.forEach {
                val player = it.key
                players.remove(player)
                resetPlayer(player)
                player.showTitle(Title.title(Component.text("Game Over"), Component.text("Your team ran out of space!")))
            }
        }

    }

    private fun declareWinner(winner: Team) {
        Bukkit.broadcast(Component.text("$winner team wins!"))
        TODO("Declare winner.")
    }

    data class GamePlayer(val player: Player, val team: Team, var kills: Int, var deaths: Int)

    class Timer : Runnable {
        var timerID: Int = -1
        private var time = 10

        override fun run() {
            time -= 1
            val state = Game.state
            if (time <= 0) {
                Game.state = when (state) {
                    GameState.BUILDING -> {
                        time = 50
                        GameState.ATTACKING
                    }
                    GameState.ATTACKING, GameState.STARTING -> {
                        time = 30
                        giveBlocks()
                        GameState.BUILDING
                    }
                    else -> return
                }
            }
            players.keys.forEach { it.sendActionBar(Component.text("${time}s of ${Game.state.toString().lowercase()}.")) }
        }

    }

    fun giveBlocks() {
        players.forEach { (p, player) -> p.inventory.addItem(ItemStack(player.team.blockType, 64)
            .canPlaceOn(player.team.blockType)
            .canPlaceOn(player.team.groundType)) }
    }

    fun teleportToWorldSpawn(player: Player) {
        player.teleport(Location(player.world, -2.0, 65.0, -2.0))
    }

    fun resetPlayer(player: Player) {
        teleportToWorldSpawn(player)
        player.inventory.clear()
        player.health = 20.0
    }

}
