package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.config.DataFile
import org.bukkit.entity.Player

enum class PlayerProfession {
    SWORDSMAN,
    ARCHER,
    MAGE;

    companion object {
        fun getProfession(player: Player): PlayerProfession? {
            val profession = DataFile.playerData.getString("${player.uniqueId}.profession")
            return when(profession) {
                "SWORDSMAN" -> SWORDSMAN
                "ARCHER" -> ARCHER
                "MAGE" -> MAGE
                else -> null
            }
        }
    }
}
