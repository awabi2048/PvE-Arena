package me.awabi2048.pve_arena.command

import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.game.WaveProcessingMode
import me.awabi2048.pve_arena.item.ItemManager
import me.awabi2048.pve_arena.misc.sendError
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object MainCommand : CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p0 !is Player) {
            p0.sendMessage("$prefix §cエラー: このコマンドはプレイヤーからのみ実行可能です。")
            return true
        }

        // 引数なし → メニュー開く
        if (p3.isNullOrEmpty()) {
//            val mainMenu =
//
//            // 万が一失敗したらエラー
//            if (mainMenu == null) {
//                p0.sendError("メインメニューの生成に失敗しました。")
//                return true
//            }
//
//            // メニュー開く
//            p0.openInventory(mainMenu)

            // 効果音
            p0.playSound(p0, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            return true

        }

        // 引数あり
        if (p3.isNotEmpty()) {
            val option = when (p3[0]) {
                "party" -> SubCommand.Option.PARTY
                else -> null
            }

            if (option != null) {
                SubCommand(p0, p3, option).execute()
                return true
            }

            // 権限あり
            if (p0.hasPermission("pve_arena.command.main")){
                val adminOption = when (p3[0]) {
                    "config" -> SubCommandAdmin.Option.CONFIG
                    "start_session" -> SubCommandAdmin.Option.START_SESSION
                    "join_session" -> SubCommandAdmin.Option.JOIN_SESSION
                    "stop_session" -> SubCommandAdmin.Option.STOP_SESSION
                    "get_item" -> SubCommandAdmin.Option.GET_ITEM
                    "quest_update" -> SubCommandAdmin.Option.QUEST_UPDATE
                    else -> null
                }

                if (adminOption == null) {
                    p0.sendError("無効なコマンドです。")
                    return true
                }

                SubCommandAdmin(p0, p3, adminOption).execute()
            }

            // それ以外
            p0.sendError("無効なコマンドです。")
            return true
        }
        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): List<String>? {
        if (!p0.hasPermission("pve_arena.command.main")) {
            return listOf("party")
        } else {
            if (p3.isNullOrEmpty()) return null

            if (p3.size == 1) return listOf("config", "start_session", "stop_session", "join_session", "get_item", "quest_update", "party")

            if (p3[0] == "start_session") {
                if (p3.size == 2) return listOf("NORMAL", "QUICK", "DUNGEON")
                if (p3[1] == "NORMAL") {
                    if (p3.size == 3) {
                        val mobTypeList = WaveProcessingMode.MobType.entries.toList()
                        val mobTypeStringList: MutableList<String> = mutableListOf()
                        mobTypeList.forEach {
                            mobTypeStringList += it.toString()
                        }
                        return mobTypeStringList
                    }

                    if (p3.size == 4) {
                        val difficultyList = WaveProcessingMode.MobDifficulty.entries.toList()
                        val difficultyStringList: MutableList<String> = mutableListOf()
                        difficultyList.forEach {
                            difficultyStringList += it.toString()
                        }
                        return difficultyStringList
                    }
                }

                if (p3[1] == "QUICK") {
                    return listOf("現在未実装です")
                }

                if (p3[1] == "DUNGEON") {
                    return listOf("現在未実装です")
                }
            }

            if (p3[0] == "get_item") {
                if (p3.size == 2) {
                    val list: MutableList<String> = mutableListOf()
                     ItemManager.ArenaItem.entries.forEach { list += it.toString() }
                    return list
                }
            }
        }
        return listOf()
    }
}
