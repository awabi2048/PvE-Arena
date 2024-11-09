package me.awabi2048.kota_arena.config

import me.awabi2048.kota_arena.Main
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class YamlUtil(private val filePath: String) {
    fun get(): YamlConfiguration {
        val settingDataFile = File(Main.instance.dataFolder.toString() + File.separator + filePath.replace("/", File.separator))
        return YamlConfiguration.loadConfiguration(settingDataFile)
    }

    fun save(yamlSection: YamlConfiguration): Boolean {
        try {
            val settingDataFile =
                File(Main.instance.dataFolder.toString() + File.separator + filePath.replace("/", File.separator))
            yamlSection.save(settingDataFile)

            return true
        } catch (e: Exception) {
            return false
        }
    }
}
