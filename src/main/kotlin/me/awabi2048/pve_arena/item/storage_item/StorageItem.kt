package me.awabi2048.pve_arena.item.storage_item

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class StorageItem(val item: ItemStack, val player: Player) {
    val sortableItem = listOf(
        Material.ROTTEN_FLESH,
        Material.BONE,
        Material.ARROW,
        Material.STRING,
        Material.SPIDER_EYE,
        Material.BLAZE_ROD,
        Material.PRISMARINE_SHARD,
        Material.PRISMARINE_CRYSTALS,
        Material.ENDER_PEARL,
    )

    val storageData: MutableMap<Material, Int>
        get() {
            return sortableItem.associateWith {
                item.itemMeta.persistentDataContainer.get(
                    NamespacedKey(instance, "storage.$it"),
                    PersistentDataType.INTEGER
                )!!
            }.toMutableMap()
        }

    fun release(inventory: Inventory): ItemStack {
        val emptySlotCount = inventory.contents.filter { it == null }.size

        // 空かそうでなかで音再生
        if (storageData.values.all { it == 0 }) {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.8f)
        } else {
            player.playSound(player, Sound.BLOCK_WOOD_HIT, 1.0f, 1.0f)
            player.playSound(player, Sound.BLOCK_CHEST_CLOSE, 1.0f, 2.0f)
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
        }

        // アイテム種ごとに
        for (itemType in sortableItem) {
            // 新規占有スロット数
            val occupySlot = if (storageData[itemType]!! % 64 == 0) {
                storageData[itemType]!! / 64
            } else {
                storageData[itemType]!! / 64 + 1
            }

            if (emptySlotCount >= occupySlot) { // すべてのアイテムを展開できるよ

                // スタックでいれるもの
                repeat(storageData[itemType]!! / 64) {
                    inventory.addItem(ItemStack(itemType, 64))
                }
                // 余り
                inventory.addItem(ItemStack(itemType, storageData[itemType]!! % 64))

                // データも書き換え
                storageData[itemType] = 0

            } else if (emptySlotCount >= 1) { // できないけど一部は入る
                repeat(emptySlotCount) {
                    inventory.addItem(ItemStack(itemType, 64))
                }

                storageData[itemType] = storageData[itemType]!! - emptySlotCount * 64
            }

            val itemMeta = item.itemMeta
            itemMeta.persistentDataContainer.set(
                NamespacedKey(instance, "storage.$itemType"),
                PersistentDataType.INTEGER,
                storageData[itemType]!!
            )
            item.itemMeta = itemMeta

        }

        return item
    }

    fun store(inventory: Inventory): ItemStack {
        player.playSound(player, Sound.BLOCK_WOOD_HIT, 1.0f, 1.0f)
        player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 2.0f)
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

        var unusedItemAmount = 1024

        for (itemType in sortableItem) {
            // 未使用領域・いまのスタック数取得
            unusedItemAmount -= storageData.values.sum()

            // インベントリ中の該当アイテム
            inventory.contents.filter { it != null && it.type == itemType && !it.hasItemMeta() }.forEach {

                // 完全に入る
                if (unusedItemAmount >= it!!.amount) {
                    unusedItemAmount -= it.amount

                    storageData[itemType] = storageData[itemType]!! + it.amount
                    it.amount = 0

                    // 部分的に入る
                } else {
                    val remainedAmount = unusedItemAmount - it.amount
                    storageData[itemType] = storageData[itemType]!! + unusedItemAmount
                    it.amount = remainedAmount
                }
            }

            //
            val itemMeta = item.itemMeta
            itemMeta.persistentDataContainer.set(
                NamespacedKey(instance, "storage.$itemType"),
                PersistentDataType.INTEGER,
                storageData[itemType]!!
            )
            item.itemMeta = itemMeta

        }

        return item
    }

    private fun updateLore(inventory: Inventory) {

    }
}