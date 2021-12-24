package me.zeepic.turf.util

import org.bukkit.Material
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

fun ItemStack.asAmount(amount: Int): ItemStack {
    this.amount = amount
    return this
}

fun ItemStack.canBreak(material: Material): ItemStack {
    val meta = itemMeta
    val currentKeys = meta.destroyableKeys.toMutableSet()
    currentKeys.add(material.key)
    meta.setDestroyableKeys(currentKeys)
    itemMeta = meta
    return this
}

fun ItemStack.canPlaceOn(material: Material): ItemStack {
    val meta = itemMeta
    val currentKeys = meta.placeableKeys.toMutableSet()
    currentKeys.add(material.key)
    meta.setPlaceableKeys(currentKeys)
    itemMeta = meta
    return this
}
