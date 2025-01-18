package me.awabi2048.pve_arena.quest

import me.awabi2048.pve_arena.config.DataFile
import org.bukkit.entity.Player

object WeeklyQuest: GenericQuest {
    override fun update() {
        TODO("Not yet implemented")
    }

    override fun rewardDistribute(player: Player) {
        TODO("Not yet implemented")
    }

    override fun criteriaCheck() {
        TODO("Not yet implemented")
    }

    override fun getPlayerStatus(player: Player, id: String): GenericQuest.QuestStatus {
        val status = GenericQuest.QuestStatus(DataFile.playerQuestData.getInt("${player.uniqueId}.weekly.$id.current"), DataFile.ongoingQuestData.getInt("weekly.$id.value"), DataFile.playerQuestData.getBoolean("${player.uniqueId}.weekly.$id.hasCompleted"))
        return status
    }
}
