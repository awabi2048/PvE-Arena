package me.awabi2048.pve_arena.config

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object YamlUtil {
    fun load(filePath: String): YamlConfiguration {
        val settingDataFile = File(instance.dataFolder.toString() + File.separator + filePath.replace("/", File.separator))
        return YamlConfiguration.loadConfiguration(settingDataFile)
    }

    fun save(filePath: String, yamlSection: YamlConfiguration): Boolean {
        try {
            val settingDataFile =
                File(instance.dataFolder.toString() + File.separator + filePath.replace("/", File.separator))
            yamlSection.save(settingDataFile)

            return true
        } catch (e: Exception) {
            return false
        }
    }
}
