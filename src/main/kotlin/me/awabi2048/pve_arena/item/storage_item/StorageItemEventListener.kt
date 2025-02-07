package me.awabi2048.pve_arena.item.storage_item

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.item.StorageItem
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object StorageItemEventListener: Listener {
    @EventHandler
    fun onPlayerStore(event: InventoryClickEvent) {
        if (event.clickedInventory != null) {
            val currentItem = if (event.currentItem != null) { StorageItem.getFromItem(event.currentItem!!) in StorageItem.list } else false
            val cursorItem = StorageItem.getFromItem(event.cursor) in StorageItem.list

            if (currentItem || cursorItem) {
                val player = event.whoClicked as Player
                val inventory = player.inventory

                val sortableItem = listOf(
                    Material.ROTTEN_FLESH,
                    Material.BONE,
                    Material.STRING,
                    Material.SPIDER_EYE,
                    Material.BLAZE_ROD,
                    Material.PRISMARINE_SHARD,
                    Material.PRISMARINE_CRYSTALS,
                    Material.ENDER_PEARL,
                )

                // ストレージアイテムのはなし
                val storageItem = if (event.isShiftClick && event.isRightClick && cursorItem) {
                    event.cursor
                } else if (event.isRightClick && event.cursor.type == Material.AIR && currentItem) {
                    event.currentItem!!
                } else return

                val storageData: MutableMap<Material, Int> = mutableMapOf()
                for (itemType in sortableItem) {
                    storageData[itemType] = storageItem.itemMeta.persistentDataContainer.get(
                        NamespacedKey(instance, "storage.$itemType"),
                        PersistentDataType.INTEGER
                    )!!
                }

                val emptySlotCount = event.clickedInventory!!.contents.filter { it == null }.size

                // release
                if (event.isShiftClick && event.isRightClick && cursorItem) {
                    event.isCancelled = true
                    player.playSound(player, Sound.BLOCK_WOOD_HIT, 1.0f, 1.0f)
                    player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 2.0f)
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

                    for (itemType in sortableItem) {
                        val occupySlot = if (storageData[itemType]!! % 64 == 0) {
                            storageData[itemType]!! / 64
                        } else {
                            storageData[itemType]!! / 64 + 1
                        }

                        if (emptySlotCount >= occupySlot) { // すべてのアイテムを展開できるよ

                            // スタックでいれるもの
                            repeat(storageData[itemType]!! / 64) {
                                event.clickedInventory!!.addItem(ItemStack(itemType, 64))
                            }
                            // 余り
                            event.clickedInventory!!.addItem(ItemStack(itemType, storageData[itemType]!! % 64))

                            // データも書き換え
                            storageData[itemType] = 0

                        } else if (emptySlotCount >= 1) { // できないけど一部は入る
                            repeat(emptySlotCount) {
                                event.clickedInventory!!.addItem(ItemStack(itemType, 64))
                            }

                            storageData[itemType] = storageData[itemType]!! - emptySlotCount * 64
                        }

                        val itemMeta = storageItem.itemMeta
                        itemMeta.persistentDataContainer.set(
                            NamespacedKey(instance, "storage.$itemType"),
                            PersistentDataType.INTEGER,
                            storageData[itemType]!!
                        )
                        storageItem.itemMeta = itemMeta
                    }

                    // store
                } else if (event.isRightClick && event.cursor.type == Material.AIR && currentItem) {
                    player.playSound(player, Sound.BLOCK_WOOD_HIT, 1.0f, 1.0f)
                    player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 2.0f)
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

                    event.isCancelled = true

                    var unusedItemAmount = 1024

                    for (itemType in sortableItem) {
                        // 未使用領域・いまのスタック数取得
                        storageData[itemType] = storageItem.itemMeta.persistentDataContainer.get(
                            NamespacedKey(
                                instance,
                                "storage.$itemType"
                            ), PersistentDataType.INTEGER
                        )!!
                        unusedItemAmount -= storageData[itemType]!!

                        var totalAmount = 0

                        // インベントリ中の該当アイテム
                        inventory.contents.filter { it != null && it.type == itemType && !it.hasItemMeta() }.forEach {
                            totalAmount += it!!.amount

                            // 完全に入る
                            if (unusedItemAmount >= it.amount) {
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
                        val itemMeta = storageItem.itemMeta
                        itemMeta.persistentDataContainer.set(
                            NamespacedKey(instance, "storage.$itemType"),
                            PersistentDataType.INTEGER,
                            storageData[itemType]!!
                        )
                        storageItem.itemMeta = itemMeta

                    }
                    println("$storageData")
                }
            }
        }
    }
}
