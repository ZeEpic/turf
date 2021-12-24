package me.zeepic.turf.util

import org.bukkit.util.Vector
import kotlin.random.Random


fun Vector.compare(other: Vector, function: (Double, Double) -> Double): Vector {
    return Vector(function(x, other.x), function(y, other.y), function(z, other.z))
}

fun Vector.atY(y: Double): Vector {
    val vector = clone()
    vector.y = y
    return vector
}

fun Vector.withRandomOffset(x: Int, z: Int): Vector {
    val vector = clone()
    val random = Random(System.currentTimeMillis())
    vector.add(Vector(random.nextInt(-x, x), 0, random.nextInt(-z, z)))
    return vector
}
