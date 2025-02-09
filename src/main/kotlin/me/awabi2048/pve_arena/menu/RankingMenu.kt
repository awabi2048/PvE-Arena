package me.awabi2048.pve_arena.menu

import org.bukkit.Bukkit
import org.bukkit.entity.Player

class RankingMenu(player: Player): MenuManager(player, MenuType.Ranking(null)) {
    override fun open() {
        val menu = Bukkit.createInventory(null, 54, "§8§lRanking Menu")
        for (row in 0..8) {
            menu.setItem(row, black)
            menu.setItem(row + 9, gray)
            menu.setItem(row + 18, gray)
            menu.setItem(row + 27, gray)
            menu.setItem(row + 36, gray)
            menu.setItem(row + 45, black)
        }


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
