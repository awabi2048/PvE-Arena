package me.awabi2048.kota_arena.command

import me.awabi2048.kota_arena.Main.Companion.prefix
import me.awabi2048.kota_arena.menu_manager.main.getMainMenu
//import me.awabi2048.kota_arena.arena_menu.getArenaMenu
import me.awabi2048.kota_arena.misc.sendError
import me.awabi2048.kota_arena.misc.sendPermissionError
import me.awabi2048.kota_arena.session_manager.sessionWorldPreparation
import org.bukkit.Bukkit.getWorld
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import java.util.*

object MainCommand : CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p0 !is Player) {
            p0.sendMessage("$prefix §cエラー: このコマンドはプレイヤーからのみ実行可能です。")
            return true
        }

        // 引数なし → メニュー開く
        if (p3.isNullOrEmpty()) {
            val mainMenu = getMainMenu(p0)

            // 万が一失敗したらエラー
            if (mainMenu == null) {
                sendError(p0, "メインメニューの生成に失敗しました。")
                return true
            }

            // メニュー開く
            p0.openInventory(mainMenu)

            // 効果音
            p0.playSound(p0, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            return true

        }

        // 引数あり
        if (p3.isNotEmpty()) {
            // だが権限なし → 無効なコマンド
            if (p0.hasPermission(Permission("kota_arena.command.config"))) {
                sendPermissionError(p0)
                return true
            }

            // 権限あり →


        }
        return true

    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): MutableList<String>? {
        return null
    }
}
