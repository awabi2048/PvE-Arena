package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Swordsman(val player: Player): Profession(player) {
    // 個別のスキルの処理
    private fun skillSlash() {
        Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 4.0, 1.2f)

        val deltaVec = player.eyeLocation.direction
        println("$deltaVec")
        for (delta in 0..20) {
//            val multipliedDeltaVec = deltaVec.multiply(delta * 0.1)
            val location = player.eyeLocation
            println("${deltaVec}, , $location")
//            val location = player.eyeLocation.add(Vector(direction.x * 4.0 + delta * 0.1, direction.y * 4.0 + delta * 0.05, direction.z * 4.0))
            player.world.spawnParticle(Particle.CRIT, location, 1, 0.0, 0.0, 0.0, 0.0)
        }

//        for (angle in -60..60) {
//            val locationDelta = player.eyeLocation.direction.multiply(4.0)
//            locationDelta.rotateAroundY(Math.toRadians(angle.toDouble()))
//            locationDelta.rotateAroundX(Math.toRadians(angle.toDouble() * 0.5))
//
//            val location = locationDelta.toLocation(player.world).add(player.eyeLocation)
//            println(location)
//            player.world.spawnParticle(Particle.CRIT, location, 1, 0.0, 0.0, 0.0, 0.0)
//
//            player.getNearbyEntities(10.0, 10.0, 10.0).filter {it is Monster && it.boundingBox.contains(location.toVector())}.forEach {
//                it as LivingEntity
//                it.damage(5.0)
//            }
//        }
    }

    private fun skillSlide() {
        player.velocity = player.eyeLocation.direction
        player.velocity.setY(0.1)
    }

    private fun skillRoundSlash() {
        var rotated = 0

        object: BukkitRunnable() {
            override fun run() {
                rotated += 12
                player.eyeLocation.direction.rotateAroundY(Math.toRadians(rotated.toDouble()))

                if (rotated == 360) {
                    cancel()
                }
            }
        }.runTaskTimer(instance, 0L, 1L)
    }

    private fun skillBuff() {
        Lib.playGlobalSound(player, Sound.ENTITY_WOLF_HOWL, 4.0, 1.0f)
        player.damage(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * 0.5)
        player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 30, 5))
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 3, 0))
    }

    // id -> スキル呼び出し TODO: Reflectionつかえば親クラスにまとめられるかも？
    override fun callSkillWithId(id: String) {
        when(id) {
            "slash" -> skillSlash()
            "slide" -> skillSlide()
            "round_slash" -> skillRoundSlash()
            "buff" -> skillBuff()
        }
    }
}
