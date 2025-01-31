package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Mage(val player: Player): Profession(player) {
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

    private fun skillSlide() {}

    private fun skillRoundSlash() {}

    private fun skillBuff() {
        Lib.healPlayer(player, 4.0)
        Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_LEVELUP, 4.0, 2.0f)
    }

    // id -> スキル呼び出し TODO: Reflectionつかえば親クラスにまとめられるかも？
    override fun callSkillWithId(id: String) {
        when(id) {
            "slash" -> skillSlash()
            "slide" -> skillSlide()
            "roundSlash" -> skillRoundSlash()
            "buff" -> skillBuff()
        }
    }
}
