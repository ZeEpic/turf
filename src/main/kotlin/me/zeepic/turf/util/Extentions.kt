package me.zeepic.turf.util

import org.bukkit.Location

fun Boolean.toInt() = if (this) 1 else 0

fun String.toTitle(): String {
    return replaceFirstChar { it.uppercase() }
}

fun Location.rotated(pitch: Int, yaw: Int): Location {
    this.pitch = pitch.toFloat()
    this.yaw = yaw.toFloat()
    return this
}