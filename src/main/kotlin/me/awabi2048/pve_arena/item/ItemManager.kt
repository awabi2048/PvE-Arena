package me.awabi2048.pve_arena.item

import org.bukkit.inventory.ItemStack

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
        KEY_70,
        KEY_100,
        ENTER_COST_ITEM,
        ENTER_COST_ITEM_RARE,
    }
}
