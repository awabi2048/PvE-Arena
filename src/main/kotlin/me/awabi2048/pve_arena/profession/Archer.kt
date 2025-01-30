package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class Archer(val player: Player): Profession(player) {
    private fun skillHeal(player: Player) {
        Lib.healPlayer(player, 4.0)
        Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_LEVELUP, 4.0, 2.0f)
    }

    private fun skillShoot(player: Player) {
        for (angle in listOf(-5.0, 0.0, 5.0)){ // degrees
            val projectile = player.world.spawnEntity(player.eyeLocation, EntityType.ARROW)
            projectile.velocity = player.eyeLocation.direction.rotateAroundY(Math.toRadians(angle)).multiply(3.0)
            projectile.scoreboardTags.add("explosive_arrow")
        }

        player.velocity = player.eyeLocation.direction.multiply(-1).setY(0.5)
    }

    override fun callSkill(spell: List<ProfessionSkillState.SpellClick>) {
        when (spell) {
            listOf(ProfessionSkillState.SpellClick.RIGHT, ProfessionSkillState.SpellClick.RIGHT) -> skillHeal(player)
            listOf(ProfessionSkillState.SpellClick.RIGHT, ProfessionSkillState.SpellClick.LEFT) -> skillShoot(player)
        }
    }
}
