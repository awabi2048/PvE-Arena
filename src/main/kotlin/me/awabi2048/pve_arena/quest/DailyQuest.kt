package me.awabi2048.pve_arena.quest

import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.game.WaveProcessingMode
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.time.LocalDate

object DailyQuest: GenericQuest {
    val challenge = listOf(
        GenericQuest.Criteria.CLEAR_COUNT_BOSS,
        GenericQuest.Criteria.CLEAR_TIME_QUICK
    )

    override fun update() {
        DataFile.ongoingQuestData.set("daily.date", LocalDate.now().toString())

        // ノーマル目標2つを決定、ファイルに書き込み
        val normalObjective1 = (dailyCriteria - challenge).random()
        val normalObjective2 = (dailyCriteria - challenge - normalObjective1).random()

        for (index in listOf(normalObjective1, normalObjective2).indices) {
            val objective = listOf(normalObjective1, normalObjective2)[index]
            val dataSection = getDataSection(GenericQuest.QuestType.DAILY, objective)

            val modifier = (dataSection.getInt("index_min")..dataSection.getInt("index_max")).random()
            val value = dataSection.getInt("base_value") * modifier

            DataFile.ongoingQuestData.set("daily.normal_${index + 1}.icon", dataSection.getString("icon"))
            DataFile.ongoingQuestData.set("daily.normal_${index + 1}.criteria", listOf(normalObjective1, normalObjective2)[index].name)
            DataFile.ongoingQuestData.set("daily.normal_${index + 1}.value", value)
            DataFile.ongoingQuestData.set("daily.normal_${index + 1}.reward.point", dataSection.getInt("reward.point") * modifier)
            DataFile.ongoingQuestData.set("daily.normal_${index + 1}.reward.quest_note", dataSection.getInt("reward.quest_note") * modifier)

            val criteria = when(listOf(normalObjective1, normalObjective2)[index]) {
                GenericQuest.Criteria.CLEAR_COUNT_MOB_TYPE -> {
                    val mobTypeSection = Lib.getMobTypeSection(WaveProcessingMode.MobType.entries.random())!!
                    mobTypeSection.getString("name")!!
                }

                GenericQuest.Criteria.CLEAR_COUNT_MOB_DIFFICULTY -> {
                    val mobDifficultySection = Lib.getMobDifficultySection(WaveProcessingMode.MobDifficulty.entries.random())!!
                    mobDifficultySection.getString("name")!!
                }

                else -> ""
            }

            DataFile.ongoingQuestData.set("daily.normal_${index + 1}.title", dataSection.getString("title"))
            DataFile.ongoingQuestData.set("daily.normal_${index + 1}.description", dataSection.getString("description")!!.replace("{criteria}", "$criteria§7").replace("{value}",
                "§a$value§7"
            ))

            // 数字のところを色付きにするから、「回」も色付きにしたいよね
            if (DataFile.ongoingQuestData.getString("daily.normal_${index + 1}.description")!!.contains("§7回")) DataFile.ongoingQuestData.set("daily.normal_${index + 1}.description", DataFile.ongoingQuestData.getString("daily.normal_${index + 1}.description")!!.replace("§7回", "回§7"))
        }

        // チャレンジ目標1つを決定、ファイルに書き込み
        val challengeObjective = challenge.random()
        val dataSection = getDataSection(GenericQuest.QuestType.DAILY, challengeObjective)

        val modifier = (dataSection.getInt("index_min")..dataSection.getInt("index_max")).random()
        val value = dataSection.getInt("base_value") * modifier

        DataFile.ongoingQuestData.set("daily.challenge.icon", dataSection.getString("icon"))
        DataFile.ongoingQuestData.set("daily.challenge.criteria", challengeObjective.name)
        DataFile.ongoingQuestData.set("daily.challenge.value", value)
        DataFile.ongoingQuestData.set("daily.challenge.reward.point", dataSection.getInt("reward.point") * modifier)
        DataFile.ongoingQuestData.set("daily.challenge.reward.quest_note", dataSection.getInt("reward.quest_note") * modifier)

        val criteria = ""

        DataFile.ongoingQuestData.set("daily.challenge.title", dataSection.getString("title"))
        DataFile.ongoingQuestData.set("daily.challenge.description", dataSection.getString("description")!!.replace("{criteria}", criteria).replace("{value}", value.toString()))

        // reload
        DataFile.reloadQuestData()

        // アナウンス
        Bukkit.getWorlds().forEach { it ->
            it.players.forEach {
                it.sendMessage("$prefix §e本日分のデイリークエストが更新されました！")
                it.playSound(it, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f)
            }
        }

        println("[Arena] Updated Daily quest for date: ${LocalDate.now()}")
    }

    override fun rewardDistribute(player: Player) {
        TODO("Not yet implemented")
        //
    }

    override fun criteriaCheck() {
        TODO("Not yet implemented")
    }

    override fun getPlayerStatus(player: Player, id: String): GenericQuest.QuestStatus {
        val status = GenericQuest.QuestStatus(DataFile.playerQuestData.getInt("${player.uniqueId}.daily.$id.current"), DataFile.ongoingQuestData.getInt("daily.$id.value"), DataFile.playerQuestData.getBoolean("${player.uniqueId}.daily.$id.hasCompleted"))
        return status
    }
}

