package me.awabi2048.pve_arena.config

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object DataFile {
    lateinit var mobType: YamlConfiguration
    lateinit var mobDifficulty: YamlConfiguration
    lateinit var mobDefinition: YamlConfiguration
    lateinit var config: YamlConfiguration
    lateinit var playerData: YamlConfiguration
    lateinit var stats: YamlConfiguration
    lateinit var record: YamlConfiguration
    lateinit var questCriteria: YamlConfiguration
    lateinit var playerQuestData: YamlConfiguration
    lateinit var ongoingQuestData: YamlConfiguration
    lateinit var customModelData: YamlConfiguration
    lateinit var dungeonStructure: YamlConfiguration
    lateinit var playerSkill: YamlConfiguration

    private val resourceSet = setOf(
        "stage_data/mob_type.yml",
        "stage_data/difficulty.yml",
        "stage_data/mob_definition.yml",
        "misc/quest_criteria.yml",
        "ongoing_quest.yml",
        "config.yml",
        "player_data/main.yml",
        "player_data/quest.yml",
        "player_data/stats.yml",
        "player_data/record.yml",
        "misc/custom_model_data.yml",
//        "dungeon/structure.yml",
        "misc/player_skill.yml",
    )

    fun loadAll() {
        mobType = YamlUtil.load("stage_data/mob_type.yml")
        mobDifficulty = YamlUtil.load("stage_data/difficulty.yml")
        mobDefinition = YamlUtil.load("stage_data/mob_definition.yml")
        config = YamlUtil.load("config.yml")
        playerData = YamlUtil.load("player_data/main.yml")
        stats = YamlUtil.load("player_data/stats.yml")
        questCriteria = YamlUtil.load("misc/quest_criteria.yml")
        playerQuestData = YamlUtil.load("player_data/quest.yml")
        ongoingQuestData = YamlUtil.load("ongoing_quest.yml")
        record = YamlUtil.load("player_data/record.yml")
        customModelData = YamlUtil.load("misc/custom_model_data.yml")
//        dungeonStructure = YamlUtil.load("misc/custom_model_data.yml")
        playerSkill = YamlUtil.load("misc/player_skill.yml")
    }

    fun copy() {
        for (resource in resourceSet) {
//            println("checking: ${File(instance.dataFolder,File.separator + resource.replace("/", File.separator)).path}, exists: ${File(instance.dataFolder,File.separator + resource.replace("/", File.separator)).exists()}")

            if (!File(instance.dataFolder,File.separator + resource.replace("/", File.separator)).exists()) {
                instance.saveResource(resource.replace("/", File.separator), false)
            }
        }
    }

    fun reloadPlayerData() {
        YamlUtil.save("player_data/main.yml", playerData)
        playerData = YamlUtil.load("player_data/main.yml")

        YamlUtil.save("player_data/quest.yml", playerQuestData)
        playerData = YamlUtil.load("player_data/quest.yml")
    }

    fun reloadQuestData() {
        YamlUtil.save("ongoing_quest.yml", ongoingQuestData)
        ongoingQuestData = YamlUtil.load("ongoing_quest.yml")
    }
}
