package me.awabi2048.pve_arena.menu

import me.awabi2048.pve_arena.misc.Lib
import me.awabi2048.pve_arena.quest.GenericQuest
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class MenuManager(val player: Player, val menuType: MenuType) {
    sealed class MenuType {
        data object Main : MenuType()
        data object Entrance : MenuType()
        data class Quest(val questType: GenericQuest.QuestType) : MenuType()
    }

    val black = Lib.getHiddenItem(Material.BLACK_STAINED_GLASS_PANE)
    val gray = Lib.getHiddenItem(Material.GRAY_STAINED_GLASS_PANE)

    abstract fun open()

    fun a() {
//        val menu = Bukkit.createInventory(null, 45, "§2Arena Menu")
//        val black = Lib.getHiddenItem(Material.BLACK_STAINED_GLASS_PANE)
//        val gray = Lib.getHiddenItem(Material.GRAY_STAINED_GLASS_PANE)
//
//        for (slot in 0..8) {
//            menu.setItem(slot, black)
//            menu.setItem(slot + 9, gray)
//            menu.setItem(slot + 18, gray)
//            menu.setItem(slot + 27, gray)
//            menu.setItem(slot + 36, black)
//        }
//
//        // player data
//        val playerData = PlayerData(player)
//
//        val playerIcon = ItemStack(Material.PLAYER_HEAD)
//        val playerIconMeta = playerIcon.itemMeta
//        playerIconMeta.setItemName("§eあなたのステータス")
//        playerIconMeta.lore = listOf(
//            "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
//            "§7- §fアリーナレベル §e§lLv.§a§l${playerData.professionLevel} §7(§a${playerData.professionExpCurrent}§7/${playerData.professionExpRequired})",
//            "§7- §fアリーナポイント §e${playerData.arenaPoint} §7Point",
//            "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
//        )
//        playerIcon.itemMeta = playerIconMeta
//
//        menu.setItem(40, playerIcon)
//
//        // quest
//        val questIcon = ItemStack(Material.BAMBOO_HANGING_SIGN)
//        val questIconMeta = questIcon.itemMeta
//
//        questIconMeta.setItemName("§aクエスト")
//        questIconMeta.lore = listOf(
//            "§7クエストの確認と報酬の受け取りを行います！"
//        )
//
//        questIcon.itemMeta = questIconMeta
//
//        // shop
//        val shopIcon = ItemStack(Material.EMERALD)
//        val shopIconMeta = shopIcon.itemMeta
//
//        shopIconMeta.setItemName("§eショップ")
//        shopIconMeta.lore = listOf(
//            "チケットや"
//        )
//
//        shopIcon.itemMeta = shopIconMeta
//
//        // stats
//        val statsIcon = ItemStack(Material.CHISELED_BOOKSHELF)

        if (menuType is MenuType.Quest) {
            val menu = Bukkit.createInventory(null, 27, "§8Quest")
            val brown = Lib.getHiddenItem(Material.BROWN_STAINED_GLASS)

            for (slot in 0..8) {
                menu.setItem(slot, brown)
                menu.setItem(slot + 9, brown)
                menu.setItem(slot + 18, brown)
            }

            if (menuType.questType == GenericQuest.QuestType.OTHER) {
                val dailyIcon = ItemStack(Material.BAMBOO_HANGING_SIGN)
                val dailyIconMeta = dailyIcon.itemMeta

                dailyIconMeta.setItemName("§eデイリークエスト")
                dailyIconMeta.lore = listOf(
                    "§7本日分のデイリークエストを確認します"
                )
                dailyIcon.itemMeta = dailyIconMeta

                val weeklyIcon = ItemStack(Material.CHERRY_HANGING_SIGN)
                val weeklyIconMeta = weeklyIcon.itemMeta

                weeklyIconMeta.setItemName("§eデイリークエスト")
                weeklyIconMeta.lore = listOf(
                    "§7今週分のウィークリークエストを確認します"
                )
                weeklyIcon.itemMeta = weeklyIconMeta

                menu.setItem(11, dailyIcon)
                menu.setItem(15, weeklyIcon)

                player.playSound(player, Sound.BLOCK_WOOD_STEP, 1.0f, 1.0f)
            }

            if (menuType.questType == GenericQuest.QuestType.DAILY) {
            }

            player.openInventory(menu)
        }

        if (menuType is MenuType.Entrance) {
        }
    }
}
