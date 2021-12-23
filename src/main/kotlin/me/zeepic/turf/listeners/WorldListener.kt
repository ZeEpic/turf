package me.zeepic.turf.listeners

import me.zeepic.turf.models.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.ProjectileHitEvent

class WorldListener : Listener {

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (!Game.state.running) return
        if (!(Game has event.player)) return
        val block = event.block
        val location = block.location.clone()
        location.y = platformHeight.toDouble()
        val teamType = (Game team event.player)?.groundType ?: return
        val y = block.location.y
        if ((block.world.getType(location) != teamType) or (y > maxHeight) or (y <= platformHeight)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onHit(event: ProjectileHitEvent) {
        event.entity.remove() // to prevent lag
        if (Game.state != GameState.ATTACKING) {
            event.isCancelled = true
            return
        }
        val shooter = event.entity.shooter as Player
        if (!Game.state.running) return
        if (!(Game has shooter)) return
        val myTeam = (Game team shooter)!!
        if (event.hitBlock == null) {
            val entity = event.hitEntity
            if (entity !is Player) return
            if (!(Game has entity)) return
            val otherTeam = (Game team entity)!!
            if (otherTeam == myTeam) return // no friendly fire
            Game.stat(shooter, "kills", 1)
            Game.stat(entity, "deaths", 1)
            entity.inventory.clear()
            entity.spawn(otherTeam)
            otherTeam.removeScore()
            myTeam.addScore()
        } else {
            val myMaterial = myTeam.blockType
            val block = event.hitBlock!!
            val hitMaterial = block.type
            val enemyMaterials = Team.values()
                .map { it.blockType }
                .filter { it != myMaterial }
            if (hitMaterial in enemyMaterials) {
                block.type = Material.AIR
            }
        }
    }

}