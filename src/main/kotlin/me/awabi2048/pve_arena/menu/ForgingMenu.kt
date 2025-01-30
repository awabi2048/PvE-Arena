package me.awabi2048.pve_arena.menu

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ForgingMenu(player: Player): MenuManager(player, MenuType.Forging) {
    override fun open() {
        val menu = Bukkit.createInventory(null, 45, "§7§lFusion Crafting")

        // 枠アイテム
        for (row in 0..8) {
            menu.setItem(row, black)
            menu.setItem(row + 9, gray)
            menu.setItem(row + 18, gray)
            menu.setItem(row + 27, gray)
            menu.setItem(row + 36, black)
        }

        // クラフトグリッド
        for (gridSlot in listOf(12 ,13, 14, 21, 22, 23, 30, 31, 32)) {
            menu.setItem(gridSlot, ItemStack(Material.AIR))
        }

        // 強化素材スロット
        val materialIcon = ItemStack(Material.MAGENTA_STAINED_GLASS_PANE)
        val materialIconMeta = materialIcon.itemMeta
        materialIconMeta.setItemName("§7« §6強化素材スロット §7»")
        materialIconMeta.lore = listOf(
            ""
        )
        materialIcon.itemMeta = materialIconMeta

        for (slot in listOf(10, 19, 28)) menu.setItem(slot, materialIcon)

        // 強化素材スロット
        val catalystIcon = ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
        val catalystIconMeta = catalystIcon.itemMeta
        catalystIconMeta.setItemName("§7« §6触媒素材スロット §7»")
        catalystIconMeta.lore = listOf(
            ""
        )
        catalystIcon.itemMeta = catalystIconMeta

        for (slot in listOf(16, 25, 34)) menu.setItem(slot, catalystIcon)

        // クラフト
        val craftIcon = ItemStack(Material.BEACON)
        val craftIconMeta = craftIcon.itemMeta
        craftIconMeta.setItemName("§7« §c合成 §7»")
        craftIconMeta.lore = listOf(
            "§7❃ §eコスト§7: ",
            "",
        )
        craftIcon.itemMeta = craftIconMeta
        menu.setItem(40, craftIcon)

    }
}
