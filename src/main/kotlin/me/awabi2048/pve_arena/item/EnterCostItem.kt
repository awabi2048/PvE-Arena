package me.awabi2048.pve_arena.item

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object EnterCostItem: ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        when (itemKind) {
            ArenaItem.ENTER_COST_ITEM -> {
                val item = ItemStack(Material.PRISMARINE_CRYSTALS)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§3ソウルフラグメント")
                itemMeta.lore = listOf(
                    "§7アリーナゲートへのゲートを開きます。",
                )
                itemMeta.setMaxStackSize(16)
                itemMeta.setEnchantmentGlintOverride(true)

                item.itemMeta = itemMeta
                return item
            }
            ArenaItem.ENTER_COST_ITEM_RARE -> {
                val item = ItemStack(Material.QUARTZ)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§3§lソウルエッセンス")
                itemMeta.lore = listOf(
                    "§7アリーナゲートへのゲートを開きます。",
                )

                itemMeta.setEnchantmentGlintOverride(true)
                item.itemMeta = itemMeta
                return item
            }
            else -> throw IllegalArgumentException("@item/EnterCostItem.kt: invalid itemKind specified.")
        }
    }
}
