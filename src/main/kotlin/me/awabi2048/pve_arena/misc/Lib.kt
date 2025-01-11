package me.awabi2048.pve_arena.misc

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.codehaus.plexus.util.FileUtils

object Lib {
    fun getHiddenItem(material: Material): ItemStack {
        val item = ItemStack(material, 1)
        val itemMeta = item.itemMeta
        itemMeta.isHideTooltip = true

        item.itemMeta = itemMeta
        return item
    }

    fun timeToClock(time: Int): String {
        val decimal = time % 10
        val second = ((time / 10) % 60).toString().padStart(2, '0')
        val minute = ((time / 10 / 60) % 60).toString().padStart(2, '0')

        return "$minute:$second.$decimal"
    }
}
