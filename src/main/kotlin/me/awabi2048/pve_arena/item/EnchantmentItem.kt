package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object EnchantmentItem : ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        when (itemKind) {
            ArenaItem.ENCHANTED_BOOK_INFINIMENDING -> {
                val item = ItemStack(Material.ENCHANTED_BOOK)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§3奇妙なエンチャントの本")
                itemMeta.lore = listOf(
                    "§7§o魔力を感じる....",
                )
                itemMeta.setMaxStackSize(1)
                itemMeta.setEnchantmentGlintOverride(true)

                itemMeta.addEnchant(Enchantment.INFINITY, 1, false)
                itemMeta.addEnchant(Enchantment.MENDING, 1, false)

                itemMeta.persistentDataContainer.set(
                    NamespacedKey(instance, "id"),
                    PersistentDataType.STRING,
                    itemKind.name.substringAfter("ArenaItem.")
                )

                item.itemMeta = itemMeta
                return item
            }

            else -> throw IllegalArgumentException("@item/EnchantmentItem.kt: invalid itemKind specified.")
        }
    }

    override val list: List<ArenaItem> = listOf(
        ArenaItem.ENCHANTED_BOOK_INFINIMENDING
    )
}