package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

object SwordItem : ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        when (itemKind) {
            ArenaItem.STEEL_SWORD -> {
                val item = ItemStack(Material.IRON_SWORD)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§7鋼鉄の剣")
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
            else -> throw IllegalArgumentException("@item/SwordItem.kt: invalid itemKind specified.")
        }
    }

    override val list: List<ArenaItem>
        get() = listOf(
            ArenaItem.STEEL_SWORD
        )
}
