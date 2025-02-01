package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.Main.Companion.rewardModifiers
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.item.ItemManager
import me.awabi2048.pve_arena.item.TicketItem
import me.awabi2048.pve_arena.misc.Lib
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

    fun distribute(players: Set<Player>, ticketReward: Pair<ItemManager.ArenaItem, Int>, point: Int, exp: Int) {
        val ticketItem = TicketItem.get(ticketReward.first)
        ticketItem.amount = ticketReward.second

        // announce
        players.forEach {
            it.inventory.addItem(ticketItem)

//            val playerStats = PlayerData(it)
//            playerStats.addExp(exp)
//            playerStats.addArenaPoint(point)

            it.sendMessage(Lib.getBar(40, "§7"))
            it.sendMessage("$prefix §e報酬を受け取りました！")
            it.sendMessage("§7→ ${ticketItem.itemMeta.itemName} §fx${ticketReward.second}")
            it.sendMessage("§7→ §eアリーナポイント §e$point §7Point")
            it.sendMessage("§7→ §aアリーナ経験値 §a$exp §7Exp")
            it.sendMessage(Lib.getBar(40, "§7"))
        }
    }
}
