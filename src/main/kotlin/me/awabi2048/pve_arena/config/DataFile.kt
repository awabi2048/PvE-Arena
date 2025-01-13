package me.awabi2048.pve_arena.config

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.configuration.ConfigurationSection
import java.io.File

object DataFile {
    lateinit var mobType: ConfigurationSection
    lateinit var difficulty: ConfigurationSection
    lateinit var mobDefinition: ConfigurationSection
    lateinit var config: ConfigurationSection
    lateinit var playerData: ConfigurationSection
    lateinit var customModelData: ConfigurationSection

    private val resourceSet = setOf(
        "stage_data/mob_type.yml",
        "stage_data/difficulty.yml",
        "stage_data/mob_definition.yml",
        "config.yml",
        "player_data/main.yml",
        "player_data/quest.yml",
        "player_data/stats.yml",
        "misc/custom_model_data.yml",
    )

    fun loadAll() {
        mobType = YamlUtil.load("stage_data/mob_type.yml")
        difficulty = YamlUtil.load("stage_data/difficulty.yml")
        mobDefinition = YamlUtil.load("stage_data/mob_definition.yml")
        config = YamlUtil.load("config.yml")
        playerData = YamlUtil.load("player_data/main.yml")
        customModelData = YamlUtil.load("misc/custom_model_data.yml")
    }

    fun copy() {
        for (resource in resourceSet) {
            println("checking: ${File(instance.dataFolder,File.separator + resource.replace("/", File.separator)).path}, exists: ${File(instance.dataFolder,File.separator + resource.replace("/", File.separator)).exists()}")

            if (!File(instance.dataFolder,File.separator + resource.replace("/", File.separator)).exists()) {
                instance.saveResource(resource.replace("/", File.separator), false)
            }
        }
    }

    fun reloadPlayerData() {
        playerData = YamlUtil.load("player_data" + File.separator + "main.yml")
    }
}
