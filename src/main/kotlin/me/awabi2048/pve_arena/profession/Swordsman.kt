package me.awabi2048.pve_arena.profession

import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class Swordsman(val player: Player): Profession(player) {
    enum class Skill {
        HEAL;

        companion object {
            fun heal(player: Player) {
                player.heal(2.0)
                player.getNearbyEntities(5.0, 5.0, 5.0).filterIsInstance<Player>().forEach {
                    player.playSound(it, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f)
                }
            }
        }
    }

    fun callSkill(spell: List<ClickType>) {
        val skill = when (spell) {
            listOf(ClickType.RIGHT, ClickType.RIGHT, ClickType.RIGHT) -> Skill.HEAL
            else -> null
        }!!

        when (skill) {
            Skill.HEAL -> Skill.heal(player)
        }
    }
}
