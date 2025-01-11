package me.awabi2048.pve_arena.misc

import me.awabi2048.pve_arena.Main.Companion.prefix
import org.bukkit.entity.Player

fun Player.sendError(error: String) {
    player!!.sendMessage("$prefix §cエラー: $error")
}

