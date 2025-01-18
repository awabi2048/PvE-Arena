package me.awabi2048.pve_arena.quest

import me.awabi2048.pve_arena.config.DataFile
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

interface GenericQuest {
    enum class Criteria {
        CLEAR_COUNT_MOB_TYPE,
        CLEAR_COUNT_MOB_DIFFICULTY,
        CLEAR_COUNT_QUICK,
        CLEAR_COUNT_BOSS,
        CLEAR_COUNT_ALL,
        CLEAR_TIME_QUICK,
        GAIN_EXP,
        SLAY_MOB,
    }

    val dailyCriteria
        get() = listOf(
            Criteria.CLEAR_COUNT_MOB_TYPE,
            Criteria.CLEAR_COUNT_MOB_DIFFICULTY,
            Criteria.CLEAR_COUNT_QUICK,
            Criteria.CLEAR_COUNT_BOSS,
            Criteria.CLEAR_COUNT_ALL,
            Criteria.CLEAR_TIME_QUICK,
            Criteria.GAIN_EXP,
            Criteria.SLAY_MOB
        )
    val weeklyCriteria
        get() = listOf(
            Criteria.CLEAR_COUNT_MOB_TYPE,
            Criteria.CLEAR_COUNT_QUICK,
            Criteria.CLEAR_COUNT_BOSS,
            Criteria.CLEAR_COUNT_ALL,
            Criteria.GAIN_EXP,
        )

    enum class QuestType {
        DAILY,
        WEEKLY,
        OTHER;
    }

    fun update()
    fun rewardDistribute(player: Player)
    fun criteriaCheck()
    fun getPlayerStatus(player: Player, id: String): QuestStatus

    fun getDataSection(type: QuestType, criteria: Criteria): ConfigurationSection {
        val section = DataFile.questCriteria.getConfigurationSection("${type.name.substringBefore("QuestType.").lowercase()}.preset.${criteria.name.substringBefore("Criteria.").lowercase()}")!!
        return section
    }

    data class QuestStatus(val current: Int, val objective: Int, val hasCompleted: Boolean)
}
