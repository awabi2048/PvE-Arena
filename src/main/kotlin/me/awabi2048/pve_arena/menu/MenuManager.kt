package me.awabi2048.pve_arena.menu

import me.awabi2048.pve_arena.misc.Lib
import me.awabi2048.pve_arena.quest.GenericQuest
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class MenuManager(val player: Player, val menuType: MenuType) {
    sealed class MenuType {
        data object Main : MenuType()
        data object Entrance : MenuType()
        data class Quest(val questType: GenericQuest.QuestType?) : MenuType()
        data class Ranking(val display: RankingMenu.Subject?): MenuType()
        data object Forging: MenuType()
        data object Party: MenuType()
    }

    val black = Lib.getHiddenItem(Material.BLACK_STAINED_GLASS_PANE)
    val gray = Lib.getHiddenItem(Material.GRAY_STAINED_GLASS_PANE)
    val returnIcon = getBackIcon()

    abstract fun open()

    private fun getBackIcon(): ItemStack {
        val item = ItemStack(Material.RED_STAINED_GLASS_PANE)
        val itemMeta = item.itemMeta

        itemMeta.setItemName("§e戻る")
        item.itemMeta = itemMeta
        return item
    }
}
