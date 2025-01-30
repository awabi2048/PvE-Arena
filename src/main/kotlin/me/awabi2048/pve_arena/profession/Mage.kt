package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Mage(val player: Player): Profession(player) {
    private fun skillHeal(player: Player) {
        Lib.healPlayer(player, 4.0)
        Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_LEVELUP, 4.0, 2.0f)
    }

    private fun skillBuff(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 10, 1))
        Lib.playGlobalSound(player, Sound.BLOCK_BEACON_ACTIVATE, 3.0, 2f)
    }

    override fun callSkill(spell: List<ProfessionSkillState.SpellClick>) {
        when (spell) {
            listOf(ProfessionSkillState.SpellClick.RIGHT, ProfessionSkillState.SpellClick.RIGHT) -> skillHeal(player)
            listOf(ProfessionSkillState.SpellClick.RIGHT, ProfessionSkillState.SpellClick.LEFT) -> skillBuff(player)
        }
    }
}
