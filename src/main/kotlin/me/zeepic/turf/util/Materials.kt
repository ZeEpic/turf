package me.zeepic.turf.util

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.util.Vector


fun Material.fill(world: World, start: Vector, end: Vector) {
    val max = start.compare(end, Math::max)
    val min = start.compare(end, Math::min)
    for (x in min.blockX..max.blockX)
        for (y in min.blockY..max.blockY)
            for (z in min.blockZ..max.blockZ)
                world.setType(x, y, z, this)
}

fun Material.fillCentered(world: World, center: Vector, size: Vector) {
    fill(world, center.clone().subtract(size), center.clone().add(size))
}
