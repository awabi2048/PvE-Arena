package me.awabi2048.pve_arena.menu

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.quest.GenericQuest
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent

object MenuEventListener : Listener {
    @EventHandler
    fun onMenuClicked(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) return
        // 入場メニュー
        if (event.clickedInventory?.any { it != null && it.itemMeta.itemName == "§cゲートを開く" } == true) {
            event.isCancelled = true
            if (event.whoClicked.scoreboardTags.contains("arena.misc.in_click_interval")) return

            val menu = EntranceMenu(event.whoClicked as Player)
            val inverted = event.isRightClick
            val player = event.whoClicked as Player

            if (event.slot in listOf(19, 21, 23, 25, 40)) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            }

            // on start
            if (event.slot == 40) {
                player.closeInventory()

                menu.openGate(event.clickedInventory!!)

            } else {

                // precession
                when (event.slot) {
                    19 -> menu.cycleOption(event.inventory, EntranceMenu.OptionCategory.MobType, inverted)
                    21 -> menu.cycleOption(event.inventory, EntranceMenu.OptionCategory.MobDifficulty, inverted)
                    23 -> menu.changeOptionValue(event.inventory, EntranceMenu.OptionCategory.PlayerCount, inverted)
                    25 -> menu.changeOptionValue(event.inventory, EntranceMenu.OptionCategory.SacrificeAmount, inverted)
                }

                //
                player.scoreboardTags.add("arena.misc.in_click_interval")
                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        player.scoreboardTags.remove("arena.misc.in_click_interval")
                    },
                    3L
                )
            }
        }

        // クエストメニュー
        if (event.clickedInventory?.any { it != null && it.itemMeta.itemName == "§7« §6これまでのクリア実績 §7»" } == true) {
            event.isCancelled = true

            val slot = event.slot
            val player = event.whoClicked as Player

            if (event.clickedInventory?.any { it != null && it.itemMeta.itemName == "§aデイリークエスト" } == true) {
                if (slot !in listOf(20, 24, 41)) return
                when (slot) {
                    20 -> {
                        val menu = QuestMenu(player, GenericQuest.QuestType.DAILY)
                        menu.open()
                    }

                    24 -> {
                        val menu = QuestMenu(player, GenericQuest.QuestType.WEEKLY)
                        menu.open()
                    }
                    41 -> TODO()
                }
            }

            // daily quest
            if (event.clickedInventory?.getItem(4)?.type == Material.DIAMOND_SWORD) {
                if (slot !in listOf(20, 22, 24, 36)) return
                when (slot) {
                    20 -> TODO()
                    22 -> TODO()
                    24 -> TODO()
                    36 -> QuestMenu(player, GenericQuest.QuestType.OTHER).open()
                }
            }

            if (event.clickedInventory?.getItem(4)?.type == Material.GOLDEN_SWORD) {
            }
        }
    }

    @EventHandler
    fun onMenuOpen(event: PlayerInteractAtEntityEvent) {
        if (!event.rightClicked.scoreboardTags.contains("arena.interaction")) return
        event.isCancelled = true

        val interactionTag = event.rightClicked.scoreboardTags

        // 入場メニュー
        if (interactionTag.contains("arena.misc.game.entrance_menu") &&
            !event.player.scoreboardTags.contains("arena.misc.in_click_interval")
        ) {
            val menu = EntranceMenu(event.player)
            menu.open()
        }

        // クエストメニュー
        if (interactionTag.contains("arena.misc.quest_menu")) {
            val menu = QuestMenu(event.player, GenericQuest.QuestType.OTHER)
            menu.open()
        }

    }
}
