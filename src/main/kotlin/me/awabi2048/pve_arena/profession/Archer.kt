package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class Archer(val player: Player) : Profession(player) {
    // 個別のスキルの処理
    private fun skillExplosiveArrow() {
        Lib.playGlobalSound(player, Sound.ENTITY_ARROW_SHOOT, 4.0, 0.75f)
        Lib.playGlobalSound(player, Sound.ITEM_FIRECHARGE_USE, 4.0, 1.0f)

        for (angle in listOf(-10.0, 0.0, 10.0)) {
            val arrow = player.world.spawnEntity(player.eyeLocation.add(0.0, -0.25, 0.0), EntityType.ARROW)
            arrow.velocity = player.location.direction.rotateAroundY(Math.toRadians(angle)).multiply(3.0)
            arrow.fireTicks = 1200
            arrow.scoreboardTags.add("explosive_arrow")
        }

        player.velocity = player.eyeLocation.direction.setY(0.0).normalize().multiply(-0.5).setY(0.5)
    }

    private fun skillArrowRain() {
        Lib.playGlobalSound(player, Sound.ENTITY_ARROW_SHOOT, 4.0, 0.75f)
        Lib.playGlobalSound(player, Sound.ENTITY_DOLPHIN_SPLASH, 4.0, 0.5f)

        val deltaVec = player.eyeLocation.direction.setY(0.0).normalize().multiply(0.5)

        val location = player.location.add(0.0, 3.0, 0.0).add(deltaVec)
        val startLocation = player.location.add(0.0, 3.0, 0.0).add(deltaVec)

        object : BukkitRunnable() {
            override fun run() {
                location.add(deltaVec)

                for (i in 1..5) {
                    val arrow = player.world.spawnEntity(
                        location, EntityType.ARROW
                    )

                    val biased = arrow.location.add(
                        (-15..15).random() * 0.1,
                        (-15..15).random() * 0.1,
                        (-15..15).random() * 0.1
                    )

                    arrow.teleport(biased)

                    arrow.velocity = Vector(0.0, -0.5, 0.0)
                    (arrow as Arrow).shooter = player
                    arrow.pickupStatus = AbstractArrow.PickupStatus.CREATIVE_ONLY
                    arrow.damage = 6.0
                }

                if (location.distance(startLocation) >= 10) cancel()
            }
        }.runTaskTimer(instance, 0L, 1L)
    }

    private fun skillArrowVengeance() {
        Lib.playGlobalSound(player, Sound.ENTITY_WITHER_BREAK_BLOCK, 4.0, 1.0f)
        Lib.playGlobalSound(player, Sound.ENTITY_BLAZE_HURT, 4.0, 0.75f)

        player.world.entities.filter { it is Arrow && it.shooter is Monster }.forEach {
            it as Arrow
            val shooter = it.shooter as Monster
            shooter.damage(it.damage * 2)
            shooter.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 200, 1))

            player.world.spawnParticle(Particle.SOUL, shooter.location.add(0.0, 0.5, 0.0), 5, 0.2, 0.2, 0.2, 0.1)

            it.remove()
        }
    }

    private fun skillBuff() {
        val nearbyArrowCount =
            player.getNearbyEntities(3.0, 3.0, 3.0).filter { it is Arrow && it.shooter != player }.size
        if (nearbyArrowCount > 0) {
            Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_LEVELUP, 4.0, 1.5f)
            Lib.playGlobalSound(player, Sound.ENTITY_SKELETON_AMBIENT, 4.0, 1.5f)

            player.getNearbyEntities(3.0, 3.0, 3.0).filter { it is Arrow && it.shooter != player }
                .forEach { it.remove() }

            Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_LEVELUP, 4.0, 2.0f)
            player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, nearbyArrowCount * 20, 2))
            Lib.healPlayer(player, nearbyArrowCount.toDouble())
        } else {
            player.health -= 5.0
            Lib.playGlobalSound(player, Sound.ENTITY_SKELETON_DEATH, 4.0, 0.5f)
        }
    }

    // id -> スキル呼び出し TODO: Reflectionつかえば親クラスにまとめられるかも？
    override fun callSkillWithId(id: String) {
        when (id) {
            "explosive_arrow" -> skillExplosiveArrow()
            "arrow_rain" -> skillArrowRain()
            "arrow_vengeance" -> skillArrowVengeance()
            "buff" -> skillBuff()
        }
    }
}
