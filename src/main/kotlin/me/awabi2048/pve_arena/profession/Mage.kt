package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Mage(val player: Player): Profession(player) {
    enum class Skill {
        HEAL,
        METEOR,
        BUFF;

        companion object {
            fun heal(player: Player) {
                Lib.healPlayer(player, 5.0)

                Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_LEVELUP, 2.0f)
                Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_SPLASH, 1.2f)
            }

            fun meteor(player: Player) {
                player.sendMessage("meteor")
                Lib.playGlobalSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1.0f)
            }

            fun buff(player: Player) {
                player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 10, 1))
                Lib.playGlobalSound(player, Sound.BLOCK_BEACON_ACTIVATE, 2f)
            }
        }
    }

    fun callSkill(spell: List<ClickType>) {
        val skill = when(spell) {
            listOf(ClickType.RIGHT, ClickType.RIGHT, ClickType.RIGHT) -> Skill.HEAL
            listOf(ClickType.LEFT, ClickType.LEFT, ClickType.RIGHT) -> Skill.BUFF
            listOf(ClickType.RIGHT, ClickType.RIGHT, ClickType.LEFT) -> Skill.METEOR
            else -> null
        }!!

        when(skill) {
            Skill.HEAL -> Skill.heal(player)
            Skill.METEOR -> Skill.meteor(player)
            Skill.BUFF -> Skill.buff(player)
        }
    }
}
