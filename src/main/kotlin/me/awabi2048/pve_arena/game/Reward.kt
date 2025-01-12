package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.Main.Companion.rewardModifiers
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.misc.PlayerData
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object Reward {
    object RewardMultiplier {
        val ticket = calcTicket()
        val point = calcPoint()
        val professionExp = calcProfessionExp()

        private fun calcTicket(): Double {
            var product = 1.0
            for (modifier in rewardModifiers) {
                product *= modifier.ticket
            }
            return product
        }

        private fun calcPoint(): Double {
            var product = 1.0
            for (modifier in rewardModifiers) {
                product *= modifier.point
            }
            return product
        }

        private fun calcProfessionExp(): Double {
            var product = 1.0
            for (modifier in rewardModifiers) {
                product *= modifier.professionExp
            }
            return product
        }

        data class RewardModifier(
            val ticket: Double,
            val point: Double,
            val professionExp: Double
        )
    }

    fun getTicketItem(ticketType: TicketType): ItemStack {
        val item = ItemStack(Material.PAPER)
        val itemMeta = item.itemMeta

        val name = when (ticketType) {
            TicketType.EASY -> "§6アリーナチケット§a【初級】"
            TicketType.NORMAL -> "§6アリーナチケット§e【中級】"
            TicketType.HARD -> "§6アリーナチケット§c【上級】"
            TicketType.EXTREME -> "§6アリーナチケット§d【最上級】"
            TicketType.BOSS -> "§3ボスアリーナチケット"
        }

        itemMeta.setItemName(name)
        itemMeta.lore = listOf("§7アリーナのショップでアイテムと交換しよう！", "§f上級なチケットは、より高いレアリティのアイテムと交換できます！")
        val customModelData = when (ticketType) {
            TicketType.EASY -> 0
            TicketType.NORMAL -> 0
            TicketType.HARD -> 0
            TicketType.EXTREME -> 0
            TicketType.BOSS -> 0
        }

        if (ticketType == TicketType.EXTREME) itemMeta.setEnchantmentGlintOverride(true)

        itemMeta.setCustomModelData(customModelData)
        item.itemMeta = itemMeta
        return item
    }

    fun distribute(players: Set<Player>, ticketReward: Pair<TicketType, Int>, point: Int, exp: Int) {
        val ticketItem = getTicketItem(ticketReward.first)
        ticketItem.amount = ticketReward.second

        // announce
        players.forEach {
            it.inventory.addItem(ticketItem)

            val playerStats = PlayerData(it)
            playerStats.addExp(exp)
            playerStats.addArenaPoint(point)

            it.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
            it.sendMessage("$prefix §e報酬を受け取りました！")
            it.sendMessage("§7▶ ${ticketItem.itemMeta.itemName} §fx${ticketReward.second}")
            it.sendMessage("§7▶ アリーナポイント §e$point §7Point")
            it.sendMessage("§7▶ アリーナ経験値 §a$exp §7Exp")
            it.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
        }
    }

    enum class TicketType {
        EASY,
        NORMAL,
        HARD,
        EXTREME,
        BOSS;
    }
}
