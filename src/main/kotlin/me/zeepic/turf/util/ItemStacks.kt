package me.zeepic.turf.util

import me.zeepic.turf.Main
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack


fun ItemStack.withEnchantment(enchantment: Enchantment, level: Int): ItemStack {
    addUnsafeEnchantment(enchantment, level)
    return this
}

fun ItemStack.unbreakable(): ItemStack {
    val meta = itemMeta
    meta.isUnbreakable = true
    itemMeta = meta
    return this
}

fun ItemStack.canBreak(material: Material): ItemStack {
    val meta = itemMeta
    meta.destroyableKeys.add(NamespacedKey(Main.instance, material.toString().lowercase()))
    return this
}

fun ItemStack.canPlaceOn(material: Material): ItemStack {
    val meta = itemMeta
    meta.placeableKeys.add(NamespacedKey(Main.instance, material.toString().lowercase()))
    return this
}
