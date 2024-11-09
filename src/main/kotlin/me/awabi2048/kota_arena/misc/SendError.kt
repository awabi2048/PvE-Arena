package me.awabi2048.kota_arena.misc

import me.awabi2048.kota_arena.Main.Companion.prefix
import org.bukkit.entity.Player

class SendError(val player: Player) {
    fun notEnoughPermission() {
        player.sendMessage("$prefix §cエラー: 権限がありません。")
    }

    fun general(content: String) {
        player.sendMessage("$prefix §cエラー: ($content§c). スタッフに報告してください。")
    }
}
