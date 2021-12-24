package me.zeepic.turf.models

import me.zeepic.turf.Main
import me.zeepic.turf.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

var maxHeight = 80
var width = 25
var platformHeight = 64
var startFromEnd = 10
const val maxScore = 150

fun Player.spawn(team: Team) {
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

    var state = GameState.IDLE
    private val players = mutableMapOf<Player, GamePlayer>()
    private val timer = Timer()
    lateinit var world: World

    infix fun add(player: Player): Team {
        val team = Team.random()
        players[player] = GamePlayer(player, team, mapOf("kills" to 0, "deaths" to 0))
        return team
    }
    infix fun remove(player: Player) { players.remove(player) }
    infix fun has(player: Player) = players.contains(player)
    infix fun team(player: Player) = players[player]?.team
    infix fun reset(player: Player) {
        teleportToWorldSpawn(player)
        player.inventory.clear()
        player.health = 20.0
    }
    fun clearPlayers() = players.clear()
    fun remainingTeams() = players.map { it.value.team } .toSet()
    fun teamPlayers(team: Team) = players.filterValues { it.team == team }
    fun stat(player: Player, value: String, amount: Int) = players[player]!!.changeStat(value, amount)
    fun declareWinner(winner: Team) {
        Bukkit.broadcast(Component.text("$winner team wins!").color(NamedTextColor.DARK_GREEN))
        state = GameState.ENDING
        Bukkit.getOnlinePlayers().forEach(Game::reset)
        clearPlayers()
    }
    fun giveBlocks() {
        players.forEach { (p, player) ->
            p.inventory.remove(player.team.blockType)
            p.inventory.addItem(player.team.getPlaceableBlock().asAmount(64))
        }
    }
    fun teleportToWorldSpawn(player: Player) {
        player.teleport(Location(player.world, -2.0, 65.0, -2.0))
    }


    private class Timer : Runnable {
        var timerID: Int = -1
        private var time = 10

        override fun run() {
            time -= 1
            if (time <= 0) {
                state = when (state) {
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
            players.keys.forEach { it.sendActionBar(Component.text("${time}s of ${state.toString().lowercase()}.")) }
        }

        fun resetTime() {
            time = 10
        }

    }

    data class GamePlayer(val player: Player, val team: Team, val statMap: Map<String, Int>) {

        fun changeStat(value: String, amount: Int) {
            if (value !in statMap) return
            statMap[value]!!.plus(amount)
        }

    }


    fun timerID() = timer.timerID
    fun scheduleTimer() {
        timer.timerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.instance, timer, 0, 20L)
    }

    fun changeMap(size: MapSize, world: World) {
        this.world = world
        maxHeight = size._maxHeight
        width = size._width
        platformHeight = size._platformHeight
        startFromEnd = size._startFromEnd
    }

    fun resetTimer() {
        Bukkit.getScheduler().cancelTask(timerID())
        timer.resetTime()
    }

}
