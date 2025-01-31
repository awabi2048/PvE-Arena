package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Archer(val player: Player): Profession(player) {
    private fun skillBuff() {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 30, 1))
        player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 30, 1))
        Lib.playGlobalSound(player, Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 4.0, 2.0f)
        Lib.playGlobalSound(player, Sound.BLOCK_BEACON_ACTIVATE, 4.0, 2.0f)
    }

    private fun skillShoot() {
        for (angle in listOf(-5.0, 0.0, 5.0)){ // degrees
            val projectile = player.world.spawnEntity(player.eyeLocation, EntityType.ARROW)
            projectile.velocity = player.eyeLocation.direction.rotateAroundY(Math.toRadians(angle)).multiply(3.0)
            projectile.scoreboardTags.add("explosive_arrow")
        }

        player.velocity = player.eyeLocation.direction.multiply(-1).setY(0.5)
    }

    private fun skillArrowRain() {
        player.eyeLocation.add(player.eyeLocation.direction.setY(player.eyeLocation.direction.y + 4.0))
    }

    override fun callSkill(spell: List<ProfessionSkillState.SpellClick>) {
        when (spell) {
            listOf(ProfessionSkillState.SpellClick.RIGHT, ProfessionSkillState.SpellClick.RIGHT) -> skillBuff()
            listOf(ProfessionSkillState.SpellClick.RIGHT, ProfessionSkillState.SpellClick.LEFT) -> skillShoot()
        }
    }
}
