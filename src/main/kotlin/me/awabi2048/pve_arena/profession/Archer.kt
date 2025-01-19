package me.awabi2048.pve_arena.profession

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class Archer(val player: Player): Profession(player) {

    enum class Skill {
        HEAL,
        SHOOT;

        companion object {
            fun heal(player: Player) {
                player.heal(4.0)
                player.getNearbyEntities(5.0, 5.0, 5.0).filterIsInstance<Player>().forEach {
                    player.playSound(it, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f)
                }
            }

            fun shoot(player: Player) {
                for (angle in listOf(-5.0, 0.0, 5.0)){
                    val projectile = player.world.spawnEntity(player.eyeLocation, EntityType.ARROW)
                    projectile.setGravity(false)
                    projectile.velocity = player.eyeLocation.direction.rotateAroundY(angle)
                }
            }
        }
    }

    fun callSkill(spell: List<ClickType>) {
        val skill = when(spell) {
            listOf(ClickType.RIGHT, ClickType.RIGHT, ClickType.RIGHT) -> Skill.HEAL
            listOf(ClickType.LEFT, ClickType.LEFT, ClickType.LEFT) ->  Skill.SHOOT
            else -> null
        }!!

        when(skill) {
            Skill.HEAL -> Skill.heal(player)
            Skill.SHOOT -> Skill.shoot(player)
        }
    }
}
