package me.awabi2048.pve_arena.menu_manager

import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuManager(val player: Player) {
    sealed class MenuType {
        data object Main: MenuType()
        data object Entrance: MenuType()
    }

    fun open() {
        val menu = Bukkit.createInventory(null, 45, "§2Arena Menu")
        val black = Lib.getHiddenItem(Material.BLACK_STAINED_GLASS_PANE)
        val gray = Lib.getHiddenItem(Material.GRAY_STAINED_GLASS_PANE)

        for (slot in 0..8) {
            menu.setItem(slot, black)
            menu.setItem(slot + 9, gray)
            menu.setItem(slot + 18, gray)
            menu.setItem(slot + 27, gray)
            menu.setItem(slot + 36, black)
        }

        val playerIcon = ItemStack(Material.PLAYER_HEAD)
        val playerIconMeta = playerIcon.itemMeta
        playerIconMeta.setItemName("§eあなたのステータス")
        playerIconMeta.lore = listOf(
            ""
        )
    }
}
