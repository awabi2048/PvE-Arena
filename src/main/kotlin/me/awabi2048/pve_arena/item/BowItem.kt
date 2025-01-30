package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object BowItem : ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        when (itemKind) {
            ArenaItem.HUNTER_BOW -> {
                val item = ItemStack(Material.BOW)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§a狩人の弓")
                itemMeta.lore = listOf(
                )
                itemMeta.setMaxStackSize(1)
                itemMeta.setEnchantmentGlintOverride(false)

                itemMeta.persistentDataContainer.set(
                    NamespacedKey(instance, "id"),
                    PersistentDataType.STRING,
                    itemKind.name.substringAfter("ArenaItem.")
                )

                itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)

                item.itemMeta = itemMeta
                return item
            }

            else -> throw IllegalArgumentException("@item/BowItem.kt: invalid itemKind specified.")
        }
    }

    override val list = listOf(
        ArenaItem.HUNTER_BOW,
    )
}
