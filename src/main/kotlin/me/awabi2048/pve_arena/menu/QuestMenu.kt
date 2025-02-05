package me.awabi2048.pve_arena.menu

import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.misc.Lib
import me.awabi2048.pve_arena.quest.DailyQuest
import me.awabi2048.pve_arena.quest.GenericQuest
import me.awabi2048.pve_arena.quest.WeeklyQuest
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt

class QuestMenu(player: Player, questType: GenericQuest.QuestType?) : MenuManager(player, MenuType.Quest(questType)) {
    override fun open() {
        val menu = Bukkit.createInventory(null, 45, "")

        for (slot in 0..8) {
            menu.setItem(slot, black)
            menu.setItem(slot + 9, gray)
            menu.setItem(slot + 18, gray)
            menu.setItem(slot + 27, gray)
            menu.setItem(slot + 36, black)
        }

        // player icon
        val playerIcon = Lib.getPlayerHead(player)
        val playerIconMeta = playerIcon.itemMeta
        playerIconMeta.setItemName("§7« §6これまでのクリア実績 §7»")
        playerIconMeta.lore = listOf(
            Lib.getBar(30, "§7"),
            "§fデイリークエスト§7: §b${DataFile.playerData.getInt("$player.quest_completed_daily")}",
            "§fウィークリークエスト§7: §b${DataFile.playerData.getInt("$player.quest_completed_weekly")}",
            Lib.getBar(30, "§7"),
        )
        playerIcon.itemMeta = playerIconMeta

        // collect all icon
        val collectRewardIcon = ItemStack(Material.CAULDRON)
        var pendingRewardsDaily = 0
        var pendingRewardsWeekly = 0

        for (id in listOf("normal_1", "normal_2", "challenge")) {
            if (DailyQuest.getPlayerStatus(player, id).current == DailyQuest.getPlayerStatus(
                    player,
                    id
                ).objective && !DailyQuest.getPlayerStatus(player, id).hasCompleted
            ) pendingRewardsDaily += 1
        }

        for (id in listOf("normal_1", "normal_2", "challenge")) {
            if (WeeklyQuest.getPlayerStatus(player, id).current == WeeklyQuest.getPlayerStatus(
                    player,
                    id
                ).objective && !WeeklyQuest.getPlayerStatus(player, id).hasCompleted
            ) pendingRewardsWeekly += 1
        }

        val collectRewardIconMeta = collectRewardIcon.itemMeta
        if (pendingRewardsDaily + pendingRewardsDaily == 0) {
            collectRewardIconMeta.setItemName("§7未受取りの報酬: §cなし")
        } else {
            collectRewardIconMeta.setItemName("§7未受取りの報酬: §a${pendingRewardsDaily + pendingRewardsDaily}個")
            collectRewardIconMeta.lore = listOf(
                "§eクリックして一括で受け取ります！"
            )
            collectRewardIcon.type = Material.CHEST
        }

        collectRewardIcon.itemMeta = collectRewardIconMeta

        //
        menu.setItem(40, playerIcon)
        menu.setItem(41, collectRewardIcon)

        when ((menuType as MenuType.Quest).questType) {

            // はじめメニュー
            null -> {
                // daily
                val dailyIcon = ItemStack(Material.BAMBOO_HANGING_SIGN)
                val dailyIconMeta = dailyIcon.itemMeta
                dailyIconMeta.setItemName("§aデイリークエスト")
                dailyIconMeta.lore =
                    listOf("§7本日分の§a§nデイリークエスト§7の確認をします。", "§7« §eクリックして開く §7»")
                dailyIcon.itemMeta = dailyIconMeta

                // weekly
                val weeklyIcon = ItemStack(Material.CHERRY_HANGING_SIGN)
                val weeklyIconMeta = weeklyIcon.itemMeta
                weeklyIconMeta.setItemName("§6ウィークリークエスト")
                weeklyIconMeta.lore =
                    listOf("§7今週分の§6§nウィークリークエスト§7の確認をします。", "§7« §eクリックして開く §7»")
                weeklyIcon.itemMeta = weeklyIconMeta

                // set item
                menu.setItem(20, dailyIcon)
                menu.setItem(24, weeklyIcon)
                menu.setItem(4, Lib.getHiddenItem(Material.SWEET_BERRIES))

                // sound
                player.playSound(player, Sound.BLOCK_WOODEN_DOOR_OPEN, 1.0f, 1.2f)
                player.playSound(player, Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1.0f, 1.2f)
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            }

            GenericQuest.QuestType.DAILY -> {
                menu.setItem(41, black)
                menu.setItem(36, returnIcon)
                menu.setItem(4, Lib.getHiddenItem(Material.DIAMOND_SWORD))

                try {
                    for (id in listOf("normal_1", "normal_2", "challenge")) {
                        val material =
                            Material.valueOf(
                                DataFile.ongoingQuestData.getString("daily.$id.icon")!!
                            )
                        val questTitle = DataFile.ongoingQuestData.getString("daily.$id.title")!!
                        val questDescription = DataFile.ongoingQuestData.getString("daily.$id.description")!!

                        val icon = ItemStack(material)

                        val current = DataFile.playerQuestData.getInt("${player.uniqueId}.daily.$id.current")
                        val objective = DataFile.ongoingQuestData.getInt("daily.$id.value")

                        val iconMeta = icon.itemMeta
                        iconMeta.setItemName("§7« $questTitle §7»")
                        iconMeta.lore = listOf(
                            Lib.getBar(40, "§7"),
                            "§7$questDescription",
                            "",
                            "§f進行度§7: ${Lib.getProgressionBar(current, objective, 30)}",
                            "　　§6${((current.toDouble() / objective) * 100).roundToInt()}% §7(§b$current§7/$objective§7)",
                            Lib.getBar(40, "§7"),
                        )

                        icon.itemMeta = iconMeta
                        val slot = when(id) {
                            "normal_1" -> 20
                            "normal_2" -> 22
                            "challenge" -> 24
                            else -> 0
                        }
                        menu.setItem(slot, icon)
                    }

                    player.openInventory(menu)
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
                    player.playSound(player, Sound.BLOCK_BAMBOO_WOOD_BREAK, 1.0f, 2.0f)

                } catch (e: Exception) {
                    throw IllegalStateException("Failed to construct arena menu. @QuestMenu#open()")
                }
            }

            GenericQuest.QuestType.WEEKLY -> {
                menu.setItem(41, black)
                menu.setItem(36, returnIcon)
                menu.setItem(4, Lib.getHiddenItem(Material.GOLDEN_SWORD))

                try {
                    for (id in listOf("normal_1", "normal_2", "challenge")) {
                        val material =
                            Material.valueOf(
                                DataFile.ongoingQuestData.getString("daily.$id.icon")!!
                            )
                        val questTitle = DataFile.ongoingQuestData.getString("daily.$id.title")!!
                        val questDescription = DataFile.ongoingQuestData.getString("daily.$id.description")!!

                        val icon = ItemStack(material)

                        val current = DataFile.playerQuestData.getInt("${player.uniqueId}.daily.$id.current")
                        val objective = DataFile.ongoingQuestData.getInt("daily.$id.value")

                        val iconMeta = icon.itemMeta
                        iconMeta.setItemName("§7« $questTitle §7»")
                        iconMeta.lore = listOf(
                            Lib.getBar(40, "§7"),
                            "§7$questDescription",
                            "",
                            "§f進行度§7: ${Lib.getProgressionBar(current, objective, 30)}",
                            "　　§6${((current.toDouble() / objective) * 100).roundToInt()}% §7(§b$current§7/$objective§7)",
                            Lib.getBar(40, "§7"),
                        )

                        icon.itemMeta = iconMeta
                        val slot = when(id) {
                            "normal_1" -> 20
                            "normal_2" -> 22
                            "challenge" -> 24
                            else -> 0
                        }
                        menu.setItem(slot, icon)
                    }

                    player.openInventory(menu)
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
                    player.playSound(player, Sound.BLOCK_BAMBOO_WOOD_BREAK, 1.0f, 2.0f)

                } catch (e: Exception) {
                    throw IllegalStateException("Failed to construct arena menu. @QuestMenu#open()")
                }
            }

            else -> {

            }
        }


        player.openInventory(menu)
    }
}
