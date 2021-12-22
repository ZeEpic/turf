package me.zeepic.turf.listeners

import me.zeepic.turf.models.Game
import me.zeepic.turf.models.GameState
import me.zeepic.turf.models.spawn
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

    @EventHandler
    fun onVoidDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        event.isCancelled = true
        if (event.cause != EntityDamageEvent.DamageCause.VOID) return
        val entity = event.entity as Player
        if (Game.players.containsKey(entity)) {
            (Game team entity)!!.teleportToSpawn(entity)
        } else {
            Game.teleportToWorldSpawn(entity)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val team = (Game team event.player) ?: return
        event.isCancelled = true
        event.player.spawn(team)
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        if (event.player.gameMode == GameMode.CREATIVE) return
        event.isCancelled = true
    }

    @EventHandler
    fun onHunger(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (Game.state == GameState.STARTING) event.isCancelled = true
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        Game.resetPlayer(player)
        event.joinMessage(Component.text("$player has joined.").color(NamedTextColor.GREEN))
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        Game.players.remove(event.player)
        event.quitMessage(Component.text("${event.player} has left.").color(NamedTextColor.RED))
    }

}