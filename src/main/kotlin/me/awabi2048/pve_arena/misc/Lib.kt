package me.awabi2048.pve_arena.misc

import me.awabi2048.pve_arena.Main.Companion.activeSession
import me.awabi2048.pve_arena.game.Generic
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scoreboard.Criteria

object Lib {
    fun getHiddenItem(material: Material): ItemStack {
        val item = ItemStack(material, 1)
        val itemMeta = item.itemMeta
        itemMeta.isHideTooltip = true

        item.itemMeta = itemMeta
        return item
    }

    fun tickToClock(tick: Int): String {
        val decimal = ((tick % 20) * 5).toString().padStart(2, '0')
        val second = ((tick / 20) % 60).toString().padStart(2, '0')
        val minute = ((tick / 20 / 60) % 60).toString().padStart(2, '0')

        return "$minute:$second.$decimal"
    }

    fun lookForSession(uuid: String): Generic? {
        return if (activeSession.any { it.uuid == uuid }) {
            activeSession.filter { it.uuid == uuid }.toList()[0]
        } else null
    }

    fun getBar(length: Int, base: String): String {
        var bar = base

        var i = 0
        while (i < length) {
            bar = "$bar▬"
            i++
        }

        return bar
    }

    class SidebarManager(player: Player) {
        fun unregister(scoreboard: String) {

        }

        fun clear(value: String) {

        }

        fun set(value: String, rank: Int) {

        }

        fun setup(title: String) {
//            val scoreboard = Bukkit.getScoreboardManager().newScoreboard.registerNewObjective(
//                "arena_scoreboard_display.${player.uniqueId}",
//                Criteria.DUMMY,
//                "§7« §c§lArena §7»"
//            )
        }

        fun get() {

        }
    }
}
