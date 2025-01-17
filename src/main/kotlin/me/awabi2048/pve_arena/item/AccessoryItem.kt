package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object AccessoryItem : ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        when (itemKind) {
            ArenaItem.HUNTER_ACCESSORY -> {
                val item = ItemStack(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§b狩人のお守り")
                itemMeta.lore = listOf(
                    "§7ホットバーにあるとき、アリーナ内での攻撃力が§c§n30%上昇§7します。",
                    "§7また、攻撃の際に与えたダメージの§a§n10%§7を回復します。",
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

            else -> throw IllegalArgumentException("@item/AccessoryItem.kt: invalid itemKind specified.")
        }
    }

    override val list = listOf(
        ArenaItem.HUNTER_ACCESSORY,
    )

}
