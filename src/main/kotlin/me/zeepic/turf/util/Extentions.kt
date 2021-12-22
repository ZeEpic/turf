package me.zeepic.turf.util

import org.bukkit.util.Vector

fun Vector.compare(other: Vector, function: (Double, Double) -> Double): Vector {
    return Vector(function(x, other.x), function(y, other.y), function(z, other.z))
}

fun Boolean.toInt() = if (this) 1 else 0

fun String.toTitle(): String {
    return replaceFirstChar { it.uppercase() }
}