package me.awabi2048.pve_arena.menu

import org.bukkit.Bukkit
import org.bukkit.entity.Player

class RankingMenu(player: Player): MenuManager(player, MenuType.Ranking(null)) {
    override fun open() {
        val menu = Bukkit.createInventory(null, 54, "§8Ranking")
    }

    enum class Subject {
        COLLECTION_ZOMBIE,
        COLLECTION_SKELETON,
        COLLECTION_SPIDER,
        COLLECTION_BLAZE,
        COLLECTION_GUARDIAN,
        COLLECTION_ENDERMAN,
        COLLECTION_QUICK,
    }
}
