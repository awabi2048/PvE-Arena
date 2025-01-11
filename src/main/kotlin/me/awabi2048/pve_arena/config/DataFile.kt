package me.awabi2048.pve_arena.config

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.configuration.ConfigurationSection
import java.io.File

object DataFile {
    lateinit var mobType: ConfigurationSection
    lateinit var difficulty: ConfigurationSection
    lateinit var mobDefinition: ConfigurationSection
    lateinit var config: ConfigurationSection
    lateinit var playerData: ConfigurationSection

    fun loadAll() {
        mobType = YamlUtil.load("stage_data/mob_type.yml")
        difficulty = YamlUtil.load("stage_data/difficulty.yml")
        mobDefinition = YamlUtil.load("stage_data/mob_definition.yml")
        config = YamlUtil.load("config.yml")
        playerData = YamlUtil.load("player_data.yml")
    }

    fun copy() {
        instance.saveDefaultConfig()
        instance.saveResource("stage_data/mob_definition.yml", false)
        instance.saveResource("player_data.yml", false)
        instance.saveResource("stage_data" + File.separator + "difficulty.yml", false)
        instance.saveResource("stage_data" + File.separator + "mob_type.yml", false)
    }
}
