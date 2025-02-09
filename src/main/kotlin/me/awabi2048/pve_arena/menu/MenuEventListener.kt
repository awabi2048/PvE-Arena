package me.awabi2048.pve_arena.menu

import me.awabi2048.pve_arena.Main.Companion.activeParty
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.misc.Lib
import me.awabi2048.pve_arena.party.ChatChannel
import me.awabi2048.pve_arena.party.Party.Companion.playerChatListening
import me.awabi2048.pve_arena.party.Party.Companion.playerChatState
import me.awabi2048.pve_arena.quest.GenericQuest
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent

object MenuEventListener : Listener {
    @EventHandler
    fun onMenuClicked(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) return

        val slot = event.slot
        val player = event.whoClicked as Player

        fun menuContains(string: String): Boolean {
            return event.clickedInventory?.any { it != null && it.itemMeta.itemName == string } == true
        }
        val menuTitle = event.view.title

        // 入場メニュー
        if (menuContains("§cゲートを開く")) {
            event.isCancelled = true
            if (event.whoClicked.scoreboardTags.contains("arena.misc.in_click_interval")) return

            val menu = EntranceMenu(event.whoClicked as Player)
            val inverted = event.isRightClick

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
        if (menuContains("§7« §6これまでのクリア実績 §7»")) {
            event.isCancelled = true

            // 最初のメニュー
            if (menuContains("§aデイリークエスト")) {
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
                    36 -> QuestMenu(player, null).open()
                }
            }

            // weekly quest
            if (event.clickedInventory?.getItem(4)?.type == Material.GOLDEN_SWORD) {
            }
        }

        // パーティーメニュー
        if (menuContains("§cパーティーを解散する")) {
            event.isCancelled = true
            val party = activeParty.find { player in it.players }!!

            if (event.slot in listOf(20, 22, 24)) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

                if (party.leader != player) {
                    player.sendMessage("$prefix §cあなたはパーティーリーダーではありません。")
                    return
                }

                // 名前・説明変更
                if (slot == 20) {
                    if (event.click.isLeftClick) playerChatListening[player] = "party_setting.name"
                    if (event.click.isRightClick) playerChatListening[player] = "party_setting.description"

                    player.closeInventory()

                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
                    player.sendMessage("$prefix §7チャット欄に内容を入力し、Enterを押してください！")
                }

                if (slot == 22) {
                    if (party.isRecruiting) {
                        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.75f)
                        player.sendMessage("$prefix §fメンバー募集を§c非公開§fに変更しました。")
                    } else {
                        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
                        player.sendMessage("$prefix §fメンバー募集を§a公開§fに変更しました。")
                    }

                    party.toggleRecruiting()
                }

                if (slot == 24) {
                    party.disband()
                    player.closeInventory()
                    player.playSound(player, Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f)
                    player.sendMessage("$prefix §cパーティーを解散しました。")
                }
            }

            if (slot in listOf(38, 42)) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

                // チャット切り替え
                if (slot == 38) {
                    if (!playerChatState.contains(player)) playerChatState[player] = ChatChannel.NORMAL

                    if (playerChatState[player] == ChatChannel.NORMAL) {
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
                        player.sendMessage("$prefix §7チャットの送信先を§bパーティーチャット§7に変更しました。")
                        playerChatState[player] = ChatChannel.PARTY

                        event.currentItem!!.type = Material.LIME_DYE

                    } else if (playerChatState[player] == ChatChannel.PARTY) {
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
                        player.sendMessage("$prefix §7チャットの送信先を§fノーマルチャット§7に変更しました。")
                        playerChatState[player] = ChatChannel.NORMAL

                        event.currentItem!!.type = Material.GRAY_DYE
                    }
                }

                // プレイヤー招待
                if (slot == 42) {
                    if (party.players.size < DataFile.config.getInt("misc.game.player_max")) {
                        player.closeInventory()
                        playerChatListening[player] = "party_setting.player_invite"
                        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
                        player.sendMessage("$prefix §e招待するプレイヤーのIDを記入してください！")
                    } else {
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                        player.sendMessage("$prefix §cパーティーは満員です。")
                    }
                }
            }
        }

        // エンチャント
        if (menuContains("§c合成する")) {
            event.isCancelled = true

            if (slot !in listOf(11, 15, 40)) return
            val menu = EnchantingMenu(player)

            if (slot in listOf(11, 15)) {
                // cursorに何もなければreturn
                if (event.cursor.type == Material.AIR) return

                // スロットをcursorのアイテムで置き換え
                event.clickedInventory!!.setItem(slot, event.cursor)

                // cursorをクリア
                event.setCursor(null)

                // 合成結果をシミュレーション
                menu.simulateInfusion(event.clickedInventory!!.getItem(11)!!, event.clickedInventory!!.getItem(15)!!)

                if (menu.isPossibleInfusion) {
                    val costItem = menu.cost!!.first
                    val costExpLevel = menu.cost.second

                    val clickItem = event.clickedInventory!!.getItem(40)!!
                    val clickItemMeta = clickItem.itemMeta!!
                    clickItemMeta.lore = listOf(
                        Lib.getBar(40, "§7"),
                        "§bコスト§7: ",
                        "§7- §3§lソウルエッセンス§7: $costItem",
                        "§7- §2経験値レベル§7: $costExpLevel",
                        Lib.getBar(40, "§7"),
                    )
                    event.clickedInventory!!.setItem(40, clickItem.apply { itemMeta = clickItemMeta })
                }
            }

            if (slot == 40) {
                 if (menu.isPossibleInfusion) {
                     val resultItem = menu.infusionResult!!
                     event.clickedInventory!!.setItem(40, resultItem)
                     event.clickedInventory!!.setItem(11, null)
                     event.clickedInventory!!.setItem(15, null)
//                     player.scoreboardTags.add("arena.misc.gui_animation.enchanting")
//
//                     Bukkit.getScheduler().runTaskLater(instance, Runnable {
//                         event.isCancelled
//                         player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 0.5f)
//                     }, 10L)
//
//                     Bukkit.getScheduler().runTaskLater(instance, Runnable {
//                         player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 0.5f)
//                     }, 20L)
//
//                     Bukkit.getScheduler().runTaskLater(instance, Runnable {
//                         player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 0.5f)
//                     }, 30L)
                 } else {
                     player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 0.5f)
                 }
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
            val menu = QuestMenu(event.player, null)
            menu.open()
        }

        // enchanting メニュー
        if (interactionTag.contains("arena.misc.enchanting_menu")) {
            val menu = EnchantingMenu(event.player)
            menu.open()
        }
    }

    @EventHandler
    fun onPlayerChat(event: PlayerChatEvent) {
        if (playerChatListening[event.player] != null) {
                event.isCancelled = true
                val sender = event.player
                val party = activeParty.find { it.players.contains(event.player) }!!

                if (playerChatListening[event.player] == "party_setting.name") {
                    val newName = event.message
                    party.changeName(newName)
                }

                if (playerChatListening[event.player] == "party_setting.description") {
                    val newDescription = event.message
                    party.changeDescription(newDescription)
                }

                if (playerChatListening[event.player] == "party_setting.player_invite") {
                    val invitingPlayerName = event.message
                    val onlinePlayers = Bukkit.getOnlinePlayers().toSet()
                    val invitingPlayer = Bukkit.getPlayer(invitingPlayerName)

                    println(onlinePlayers)
                    println(invitingPlayer)

                    if (!onlinePlayers.contains(invitingPlayer) || invitingPlayer == null) {
                        sender.sendMessage("$prefix §cプレイヤーは現在オフラインであるか、ユーザー名が間違っています。")
                        sender.playSound(sender, Sound.ENTITY_CAT_PURREOW, 1.0f, 1.0f)
                    } else {
                        sender.sendMessage("$prefix §e${invitingPlayer.displayName}§7さんをパーティーに招待しました！")
                        sender.playSound(invitingPlayer, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)

                        invitingPlayer.sendMessage("$prefix §e${sender.displayName}§7さんからパーティーに招待されました！ §a/arena_party_join§7で参加します。")
                        invitingPlayer.playSound(invitingPlayer, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                    }
                }

            playerChatListening.remove(event.player)
        }
    }

    @EventHandler
    fun onPlayerMoveItem(event: InventoryClickEvent) {
//        println("slot item: ${event.currentItem}, current item: ${event.cursor}")
//        if (event.currentItem == null) return
    }
}
