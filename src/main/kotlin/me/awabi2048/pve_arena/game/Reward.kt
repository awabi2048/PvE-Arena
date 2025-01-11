package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main.Companion.rewardModifiers
import net.kyori.adventure.text.Component
import org.bukkit.Material
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

    fun getTicketItem(ticketType: String): ItemStack {
        val item = ItemStack(Material.PAPER)
        val itemMeta = item.itemMeta

        val name = when (ticketType) {
            "easy" -> "§6アリーナチケット§a【初級】"
            "normal" -> "§6アリーナチケット§e【中級】"
            "hard" -> "§6アリーナチケット§c【上級】"
            "extreme" -> "§6アリーナチケット§d【最上級】"
            else -> "§cエラー: チケット生成に失敗しました"
        }

        itemMeta.setItemName(name)
        itemMeta.lore = listOf("§7アリーナのショップでアイテムと交換しよう！", "§f上級なチケットは、より高いレアリティのアイテムと交換できます！")
        val customModelData = when (ticketType) {
            "easy" -> 0
            "normal" -> 0
            "hard" -> 0
            "extreme" -> 0
            else -> 0
        }

        if (ticketType == "extreme") itemMeta.setEnchantmentGlintOverride(true)

        itemMeta.setCustomModelData(customModelData)
        item.itemMeta = itemMeta
        return item
    }
}




