package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object EnterCostItem: ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        when (itemKind) {
            ArenaItem.ENTER_COST_ITEM -> {
                val item = ItemStack(Material.PRISMARINE_CRYSTALS)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§bソウルフラグメント")
                itemMeta.lore = listOf(
                    "§7アリーナゲートへのゲートを開きます。",
                )
                itemMeta.setMaxStackSize(32)
                itemMeta.setEnchantmentGlintOverride(true)

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, itemKind.name.substringAfter("ArenaItem."))

                item.itemMeta = itemMeta
                return item
            }
            ArenaItem.ENTER_COST_ITEM_RARE -> {
                val item = ItemStack(Material.QUARTZ)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§3ソウルエッセンス")
                itemMeta.lore = listOf(
                    "§7アリーナゲートへのゲートを開きます。",
                )

                itemMeta.setMaxStackSize(16)
                itemMeta.setEnchantmentGlintOverride(true)

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, itemKind.name.substringAfter("ArenaItem."))

                item.itemMeta = itemMeta
                return item
            }
            else -> throw IllegalArgumentException("@item/EnterCostItem.kt: invalid itemKind specified.")
        }
    }

    override val list = listOf(
        ArenaItem.ENTER_COST_ITEM,
        ArenaItem.ENTER_COST_ITEM_RARE,
    )
}
