package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType

object SacrificeItem: ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        when (itemKind) {
            ArenaItem.SACRIFICE_ITEM -> {
                val item = ItemStack(Material.GLASS_BOTTLE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§7魂入りの瓶")
                itemMeta.lore = listOf(
                    "§7亡者の声が聞こえる...",
                    "§7« §c0§7% »"
                )
                itemMeta.setMaxStackSize(1)
                (itemMeta as Damageable).setMaxDamage(10)
                itemMeta.damage = 10

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, itemKind.name.substringAfter("ArenaItem."))

                item.itemMeta = itemMeta
                return item
            }
            ArenaItem.SACRIFICE_ITEM_CHARGED -> {
                val item = ItemStack(Material.DRAGON_BREATH)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§3魂入りの瓶")
                itemMeta.lore = listOf(
                    "§7亡者の声が聞こえる...",
                    "§7« §d100§7% »"
                )
                itemMeta.setMaxStackSize(1)
                (itemMeta as Damageable).setMaxDamage(100)
                itemMeta.damage = 1

                itemMeta.setEnchantmentGlintOverride(true)

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, itemKind.name.substringAfter("ArenaItem."))

                item.itemMeta = itemMeta
                return item
            }
            else -> throw IllegalArgumentException("@item/KeyItem.kt: invalid itemKind specified.")
        }
    }

    override val list = listOf(
        ArenaItem.SACRIFICE_ITEM,
        ArenaItem.SACRIFICE_ITEM_CHARGED
    )

    fun charge(item: ItemStack): ItemStack {
        val itemMeta = item.itemMeta
        if ((itemMeta as Damageable).damage > 1){

            itemMeta.damage -= 1
            itemMeta.lore = listOf(
                "§7亡者の声が聞こえる...",
                "§7« §c${(10 - itemMeta.damage)*10}§7% »"
            )

            item.itemMeta = itemMeta
            return item
        } else {
            return get(ArenaItem.SACRIFICE_ITEM_CHARGED)
        }
    }
}
