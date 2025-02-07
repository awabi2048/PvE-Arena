package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

object StorageItem: ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        fun ItemMeta.setCommonData() {
            setMaxStackSize(1)
            (this as Damageable).setMaxDamage(10)
            damage = 0

            persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, itemKind.name.substringAfter("ArenaItem."))
        }

        fun ItemMeta.setStorageSize(value: Int) {
            persistentDataContainer.set(NamespacedKey(instance, "storage_size"), PersistentDataType.INTEGER, value)
        }

        when(itemKind) {
            ArenaItem.MOB_DROP_SACK -> {
                val item = ItemStack(Material.CAULDRON)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§aモブアイテムの入れ物")
                itemMeta.lore = listOf(
                    "§7主なモブドロップアイテムを格納します。"
                )

                itemMeta.setCommonData()
                itemMeta.setStorageSize(1024)

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "storage.ROTTEN_FLESH"), PersistentDataType.INTEGER, 0)
                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "storage.BONE"), PersistentDataType.INTEGER, 0)
                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "storage.STRING"), PersistentDataType.INTEGER, 0)
                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "storage.SPIDER_EYE"), PersistentDataType.INTEGER, 0)
                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "storage.BLAZE_ROD"), PersistentDataType.INTEGER, 0)
                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "storage.PRISMARINE_SHARD"), PersistentDataType.INTEGER, 0)
                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "storage.PRISMARINE_CRYSTALS"), PersistentDataType.INTEGER, 0)
                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "storage.ENDER_PEARL"), PersistentDataType.INTEGER, 0)

                item.itemMeta = itemMeta
                return item
            }

            ArenaItem.TICKET_SACK -> {
                val item = ItemStack(Material.BEEHIVE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§bチケットの入れ物")
                itemMeta.lore = listOf(
                    "§7主なチケットアイテムを格納します。"
                )

                itemMeta.setCommonData()
                item.itemMeta = itemMeta
                return item
            }

            else -> throw IllegalArgumentException("@item/SackItem.kt: invalid itemKind specified.")
        }
    }

    override val list: List<ArenaItem>
        get() = listOf(
            ArenaItem.MOB_DROP_SACK,
            ArenaItem.TICKET_SACK,
        )
}
