package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

object SacrificeItem: ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        when (itemKind) {
            ArenaItem.SACRIFICE_ITEM -> {
                val item = ItemStack(Material.GLASS_BOTTLE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§7魂入りのボトル")
                itemMeta.lore = listOf(
                    "§7亡者の声が聞こえる...",
                    "§7« §c0§7% »"
                )
                itemMeta.setMaxStackSize(1)
                (itemMeta as Damageable).setMaxDamage(100)
                itemMeta.damage = 100

                item.itemMeta = itemMeta
                return item
            }
            ArenaItem.SACRIFICE_ITEM_CHARGED -> {
                val item = ItemStack(Material.DRAGON_BREATH)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§3魂入りのボトル")
                itemMeta.lore = listOf(
                    "§7亡者の声が聞こえる...",
                    "§7« §d100§7% »"
                )

                itemMeta.setEnchantmentGlintOverride(true)
                item.itemMeta = itemMeta
                return item
            }
            else -> throw IllegalArgumentException("@item/KeyItem.kt: invalid itemKind specified.")
        }
    }

    fun onCharge() {

    }
}
