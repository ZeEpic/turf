package me.zeepic.turf.listeners

import me.zeepic.turf.models.Game
import me.zeepic.turf.models.GameState
import me.zeepic.turf.models.platformHeight
import me.zeepic.turf.models.spawn
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
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
        if (Game has entity) {
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
        val player = event.player
        if (player.gameMode == GameMode.CREATIVE) return
        if (!(Game has player)) return
        if (event.itemDrop.itemStack.type == (Game team player)!!.blockType) return
        event.isCancelled = true
    }

    @EventHandler
    fun onPickup(event: EntityPickupItemEvent) {
        val entity = event.entity
        if (entity !is Player) return
        if (entity.gameMode == GameMode.CREATIVE) return
        event.isCancelled = true
        if (!(Game has entity)) return
        if (event.item.itemStack.type != (Game team entity)!!.blockType) return
        event.isCancelled = false
    }

    @EventHandler
    fun onHunger(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        if (player.gameMode == GameMode.CREATIVE) return
        if (event.from.toVector() == event.to.toVector()) return
        if (Game.state == GameState.STARTING) event.isCancelled = true
        if (player.location.y > platformHeight - 10) return
        if (Game has player) {
            (Game team player)!!.teleportToSpawn(player)
        } else {
            Game.teleportToWorldSpawn(player)
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        Game reset player
        event.joinMessage(Component.text("${player.name} has joined.").color(NamedTextColor.GREEN))
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        Game remove player
        event.quitMessage(Component.text("${player.name} has left.").color(NamedTextColor.RED))
    }

    @EventHandler
    fun onShoot(event: EntityShootBowEvent) {
        event.isCancelled = !Game.state.running
    }

}