package me.awabi2048.pve_arena.menu_manager.entrance

import me.awabi2048.pve_arena.config.YamlUtil
import kotlin.math.roundToInt

class EntranceCostCalculation {
    private fun calculatePureCost(mobType: Int, difficulty: Int, playerCount: Int): Int {
        val yaml = YamlUtil.load("stage_data/mob_type.yml")

        val mobTypeSection = YamlUtil.load("stage_data/mob_type.yml")
        val difficultySection = YamlUtil.load("stage_data/difficulty.yml")

        val mobTypeKey = mobTypeSection.getKeys(false).toList()[mobType]
        val difficultyKey = difficultySection.getKeys(false).toList()[difficulty]

        val baseCost = mobTypeSection.getInt("$mobTypeKey.base_cost")
        val difficultyMultiplier = difficultySection.getDouble("$difficultyKey.mob_multiplier")

        val pureCost = (baseCost * difficultyMultiplier * playerCount).roundToInt()
        return pureCost
    }

    fun essenceCost(mobType: Int, difficulty: Int, playerCount: Int): Int {
        val pureCost = calculatePureCost(mobType, difficulty, playerCount)

        val essenceCost = pureCost / 5
        return essenceCost
    }

    fun fragmentCost(mobType: Int, difficulty: Int, playerCount: Int): Int {
        val pureCost = calculatePureCost(mobType, difficulty, playerCount)

        val fragmentCost = pureCost % 5
        return fragmentCost
    }
}
