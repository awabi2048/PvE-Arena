package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class ItemManager {
    abstract fun get(itemKind: ArenaItem): ItemStack

    enum class ArenaItem {
        TICKET_EASY,
        TICKET_NORMAL,
        TICKET_HARD,
        TICKET_EXTREME,
        TICKET_BOSS,
        REWARD_BOOST_30,
        REWARD_BOOST_50,
        EXP_BOOST_100,
        EXP_BOOST_200,
        SACRIFICE_ITEM,
        SACRIFICE_ITEM_CHARGED,
        KEY_30,
        KEY_50,
        ENTER_COST_ITEM,
        ENTER_COST_ITEM_RARE,
        HUNTER_ACCESSORY,
        ENCHANTED_BOOK_INFINIMENDING,
        WOODEN_WAND,
    }

    abstract val list: List<ArenaItem>

    fun getFromItem(item: ItemStack): ArenaItem? {
        val itemIdString =
            item.itemMeta?.persistentDataContainer?.get(NamespacedKey(instance, "id"), PersistentDataType.STRING)
                ?: return null
        return ItemManager.ArenaItem.valueOf(itemIdString)
    }

    fun searchPlayerInventory(player: Player, targetItem: ArenaItem): Int {
        val inventory = player.inventory
        var itemsFound = 0

        for (item in inventory) {
            if (item != null && getFromItem(item) != null) {
                if (getFromItem(item) == targetItem) itemsFound += item.amount
            }
        }

        return itemsFound
    }
}
