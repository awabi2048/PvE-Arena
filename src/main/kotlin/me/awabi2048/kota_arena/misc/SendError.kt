package me.awabi2048.kota_arena.misc

import me.awabi2048.kota_arena.Main.Companion.prefix
import org.bukkit.entity.Player

fun sendError(player: Player, content: String) {
    player.sendMessage("$prefix §cエラー: ($content§c). スタッフに報告してください。")
}

fun sendPermissionError(player: Player) {
    player.sendMessage("$prefix §c権限がありません。")
}
