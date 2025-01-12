package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.item.ItemManager.ArenaItem.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object TicketItem : ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        if (itemKind !in listOf(
                TICKET_EASY,
                TICKET_NORMAL,
                TICKET_HARD,
                TICKET_EXTREME,
                TICKET_BOSS,
            )
        ) throw IllegalArgumentException("@item/TicketItem.kt: itemKind should be ticket item.")

        val item = ItemStack(Material.PAPER)
        val itemMeta = item.itemMeta

        val name = when (itemKind) {
            TICKET_EASY -> "§6アリーナチケット§a【初級】"
            TICKET_NORMAL -> "§6アリーナチケット§e【中級】"
            TICKET_HARD -> "§6アリーナチケット§c【上級】"
            TICKET_EXTREME -> "§6アリーナチケット§d【最上級】"
            TICKET_BOSS -> "§3ボスアリーナチケット"
            else -> throw IllegalArgumentException("@item/TicketItem.kt: itemKind should be ticket item.")
        }

        itemMeta.setItemName(name)
        itemMeta.lore = listOf(
            "§7アリーナのショップでアイテムと交換しよう！",
            "§f上級なチケットは、より高いレアリティのアイテムと交換できます！"
        )
        val customModelData = when (itemKind) {
            TICKET_EASY -> DataFile.customModelData.getInt("arena_ticket_easy")
            TICKET_NORMAL -> DataFile.customModelData.getInt("arena_ticket_normal")
            TICKET_HARD -> DataFile.customModelData.getInt("arena_ticket_hard")
            TICKET_EXTREME -> DataFile.customModelData.getInt("arena_ticket_extreme")
            TICKET_BOSS -> DataFile.customModelData.getInt("arena_ticket_boss")
            else -> 0
        }

        itemMeta.setCustomModelData(customModelData)

        if (itemKind in listOf(TICKET_EXTREME, TICKET_BOSS)) itemMeta.setEnchantmentGlintOverride(true)

        item.itemMeta = itemMeta
        return item
    }
}
