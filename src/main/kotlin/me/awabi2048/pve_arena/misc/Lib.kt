package me.awabi2048.pve_arena.misc

import me.awabi2048.pve_arena.Main.Companion.activeSession
import me.awabi2048.pve_arena.game.Generic
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

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

    fun lookForSession(uuid: String): Generic {
        return activeSession.filter{it.uuid == uuid}.toList()[0]
    }

    fun getBar(length: Int, base: String): String {
        var i = 0
        while (i < length) {
            base.plus("▬")
            i++
        }

        return base
    }
}
