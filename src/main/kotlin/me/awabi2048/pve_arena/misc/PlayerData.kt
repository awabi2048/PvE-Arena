package me.awabi2048.pve_arena.misc

import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.config.YamlUtil
import org.bukkit.Sound
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player

class PlayerData(private val player: Player) {
    //
    private val uuid = player.uniqueId.toString()

    val professionLevel = getData("profession_level")
    val professionExpCurrent = getData("profession_exp_current")
    val professionExpRequired = getData("profession_exp_required")
    val arenaPoint = getData("arena_point")
    val questPoint = getData("quest_point")

    fun getPlayerData(): ConfigurationSection? {
        return DataFile.playerData.getConfigurationSection(uuid)
    }

    //
    fun addQuestPoint(value: Int) {
        if (getPlayerData() == null) generatePlayerData()
        DataFile.playerData.set("$uuid.quest_point", getData("quest_point") + value)
    }

    fun addArenaPoint(value: Int) {
        if (getPlayerData() == null) generatePlayerData()
        DataFile.playerData.set("$uuid.arena_point", getData("arena_point") + value)
    }

    fun addExp(value: Int) {
        if (getPlayerData() == null) generatePlayerData()

        val exp = getData("profession_exp_current")
        val expRequired = getData("profession_exp_required")

        if (exp + value >= expRequired) {
            DataFile.playerData.set("$uuid.profession_level", getData("profession_level") + 1)
            DataFile.playerData.set("$uuid.profession_exp_current", 0)
            DataFile.playerData.set("$uuid.profession_exp_required", getData("profession_exp_required") * DataFile.config.getDouble("profession_exp_multiplier"))

            player.sendMessage("$prefix §fレベルアップしました！ §7§lLv. ${getData("profession_level") - 1} §f→ §e§lLv. ${getData("profession_level")}")
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f)
        }

        YamlUtil.save("player_data.yml", DataFile.playerData as YamlConfiguration)
        DataFile.reloadPlayerData()
    }

    private fun generatePlayerData() {
        DataFile.playerData.set("$uuid.quest_point", 0)
        DataFile.playerData.set("$uuid.profession_level", 0)
        DataFile.playerData.set("$uuid.profession_exp_required", 10)
        DataFile.playerData.set("$uuid.profession_exp_current", 0)
        DataFile.playerData.set("$uuid.profession_exp_all_time", 0)

        YamlUtil.save("player_data.yml", DataFile.playerData as YamlConfiguration)
    }

    private fun getData(key: String): Int {
        if (getPlayerData() == null) generatePlayerData()
        return getPlayerData()!!.getInt(key)
    }
}
