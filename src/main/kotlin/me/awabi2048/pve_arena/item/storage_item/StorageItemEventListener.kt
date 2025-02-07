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

object StorageItemEventListener : Listener {
    @EventHandler
    fun onPlayerStore(event: InventoryClickEvent) {
        if (event.clickedInventory != null) {
            val currentItem = if (event.currentItem != null) {
                StorageItem.getFromItem(event.currentItem!!) in StorageItem.list
            } else false
            val cursorItem = StorageItem.getFromItem(event.cursor) in StorageItem.list

            if (currentItem || cursorItem) {
                val player = event.whoClicked as Player

                // ストレージアイテムのはなし
                val storageItem = if (event.click.isShiftClick && event.isRightClick && cursorItem) {
                    event.cursor
                } else if (event.click.isRightClick && event.cursor.type == Material.AIR && currentItem) {
                    event.currentItem!!
                } else return

                val storage = StorageItem(storageItem, player)

                // release
                if (event.isShiftClick && event.isRightClick && cursorItem) {
                    event.isCancelled = true
                    val item = storage.release(event.clickedInventory!!)
                    event.currentItem = item

                    // store
                } else if (event.isRightClick && event.cursor.type == Material.AIR && currentItem) {
                    event.isCancelled = true
                    storage.store(event.clickedInventory!!)
                }
                println("${storage.storageData}")
            }
        }
    }
}
