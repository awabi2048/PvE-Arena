package me.awabi2048.pve_arena.profession

import org.bukkit.entity.Player

enum class PlayerProfession {
    SWORDSMAN,
    ARCHER,
    MAGE;

    companion object {
        fun getProfession(player: Player): PlayerProfession? {
            try {
                val profession = player.getMetadata("arena_profession")
                if (profession.isEmpty()) return null
                return profession[0] as PlayerProfession
            } catch (e: Exception) {
                return null
            }
        }
    }
}
