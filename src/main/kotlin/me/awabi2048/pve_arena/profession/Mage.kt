package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class Mage(val player: Player): Profession(player) {
    enum class Skill {
        HEAL;

        companion object {
            fun heal(player: Player) {
                Lib.healPlayer(player, 5.0)



            }
        }
    }

    fun callSkill(spell: List<ClickType>) {
        val skill = when(spell) {
            listOf(ClickType.RIGHT, ClickType.RIGHT, ClickType.RIGHT) -> Skill.HEAL
            else -> null
        }!!

        when(skill) {
            Skill.HEAL -> Skill.heal(player)
        }
    }
}
