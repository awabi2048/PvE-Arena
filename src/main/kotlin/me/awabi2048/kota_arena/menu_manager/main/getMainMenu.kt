package me.awabi2048.kota_arena.menu_manager.main

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

fun getMainMenu(player: Player): Inventory? {
    try {
        val menu = Bukkit.createInventory(null, 45, "§l§2Arena Menu")

        return menu
    } catch (e: Exception) {
        return null
    }
}
