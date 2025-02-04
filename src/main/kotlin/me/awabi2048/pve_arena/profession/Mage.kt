package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class Mage(val player: Player): Profession(player) {
    // 個別のスキルの処理
    private fun skillMeteor() {
        val hitLocation = player.getTargetBlockExact(32)!!.location
        val coreLocation = hitLocation.add(0.0, 8.0, 0.0)

        val damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value
        Lib.playGlobalSound(player, Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 8.0, 0.6f)

        object: BukkitRunnable() {
            override fun run() {
                // main part
                player.world.spawnParticle(Particle.LARGE_SMOKE, coreLocation, 120, 0.5, 0.5, 0.5, 0.0)
                player.world.spawnParticle(Particle.FLAME, coreLocation, 10, 0.6, 0.6, 0.6, 0.1)
                player.world.spawnParticle(Particle.LAVA, coreLocation, 20, 0.6, 0.6, 0.6, 0.1)
                coreLocation.add(0.0, -0.5, 0.0)

                if (coreLocation.block.type != Material.AIR) {
                    player.getNearbyEntities(16.0, 16.0, 16.0).filter {it.location.distance(coreLocation) <= 6 && it is Monster}.forEach {
                        it as Monster
                        it.velocity = it.eyeLocation.toVector().subtract(coreLocation.toVector()).normalize()
                        it.damage(it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * 0.2 + damage)
                        it.fireTicks = 400
                    }

                    Lib.playGlobalSound(player, Sound.ENTITY_GENERIC_EXPLODE, 8.0, 0.8f)
                    player.world.spawnParticle(Particle.FLAME, coreLocation, 120, 0.2, 0.0, 0.2, 0.3)

                    cancel()
                }
            }
        }.runTaskTimer(instance, 0L, 1L)
    }

    private fun skillGravityWell() {

        Lib.playGlobalSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 8.0, 0.8f)
        Lib.playGlobalSound(player, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 8.0, 0.8f)
        Lib.playGlobalSound(player, Sound.BLOCK_ANVIL_USE, 8.0, 0.4f)

        val hitLocation = player.getTargetBlockExact(32)!!.location.add(0.0, 0.25, 0.0)
        var timer = 100

        object: BukkitRunnable() {
            override fun run() {
                player.world.spawnParticle(Particle.PORTAL, hitLocation, 20, 0.6, 0.6, 0.6, 0.1)
                player.world.spawnParticle(Particle.WITCH, hitLocation, 50, 3.0, 0.2, 3.0, 0.1)

                player.getNearbyEntities(16.0, 16.0, 16.0).filter {it is Monster && it.location.distance(hitLocation) <= 5.0}.forEach {
                    it.velocity = hitLocation.clone().subtract(it.location).toVector().multiply(0.08)

                    it as LivingEntity
                    it.damage(it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * 0.01 / (it.location.distance(hitLocation)))
                    it.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 60, 3))
                }

                timer -= 1
                if (timer == 0) cancel()
            }
        }.runTaskTimer(instance, 0L, 1L)
    }

    private fun skillLightning() {
        val baseLocation = player.location.add(0.0, 0.3, 0.0)
        var distance = 0.0
        player.world.spawnEntity(baseLocation, EntityType.LIGHTNING_BOLT)
        Lib.playGlobalSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 8.0, 0.6f)

        object: BukkitRunnable() {
            override fun run() {
                var angle = 0
                val delta = Vector(distance, 0.0, 0.0)

                while (angle < 360) {
                    angle += 12
                    val effectLocation = baseLocation.clone().add(delta.clone().rotateAroundY(Math.toRadians(angle + distance * 10)))

                    player.world.spawnParticle(Particle.ELECTRIC_SPARK, effectLocation, 10, 0.1, 0.1, 0.1, 0.0)
                    player.getNearbyEntities(16.0, 16.0, 16.0).filter {it is Monster && it.location.distance(effectLocation) <= 2.0}.forEach {
                        it as LivingEntity
                        it.damage(it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * 0.3)
                        player.world.spawnEntity(it.location, EntityType.LIGHTNING_BOLT)
                    }
                }
                distance += 0.5

                if (distance > 16.0) cancel()
            }
        }.runTaskTimer(instance, 0L, 1L)
    }

    private fun skillBuff() {
        val effects = player.activePotionEffects
        player.world.players.filter{it != player}.forEach {
            it.activePotionEffects += effects
        }

        player.activePotionEffects.forEach {
            player.addPotionEffect(PotionEffect(it.type, it.duration * 2, it.amplifier))
        }

        Lib.healPlayer(player, player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * (1 / player.health))
        Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 4.0, 2.0f)
        player.world.spawnParticle(Particle.TOTEM_OF_UNDYING, player.location.add(0.0, 1.0, 0.0), 50, 0.5, 0.2, 0.5, 0.2)
    }

    // id -> スキル呼び出し TODO: Reflectionつかえば親クラスにまとめられるかも？
    override fun callSkillWithId(id: String) {
        when(id) {
            "meteor" -> skillMeteor()
            "gravity_well" -> skillGravityWell()
            "lightning" -> skillLightning()
            "buff" -> skillBuff()
        }
    }
}
