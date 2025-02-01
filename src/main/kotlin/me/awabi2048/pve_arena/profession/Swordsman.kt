package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.sin

class Swordsman(val player: Player): Profession(player) {
    // 個別のスキルの処理
    private fun skillSlash() {
        fun castSlash() {
            Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 4.0, 1.2f)
            Lib.playGlobalSound(player, Sound.ENTITY_WITHER_BREAK_BLOCK, 4.0, 1.4f)

            val deltaY = sin((-10..10).random().toDouble())

            // もうわからん
            val deltaVecRotated =
                player.eyeLocation.direction.setY(-deltaY).normalize().rotateAroundY(Math.toRadians(-90.0))
                    .multiply(0.1)
            val location = player.eyeLocation.add(player.eyeLocation.direction.normalize().multiply(4.5)).add(
                player.eyeLocation.direction.setY(deltaY).normalize().rotateAroundY(Math.toRadians(90.0)).multiply(3.0)
            )

            for (delta in 0..60) {
                location.add(deltaVecRotated)
                player.world.spawnParticle(Particle.SWEEP_ATTACK, location, 2, 0.25, 0.25, 0.25, 0.5)

                player.getNearbyEntities(10.0, 10.0, 10.0)
                    .filter { it.location.distance(location) <= 2.0 && it is Monster }.forEach {
                    it as LivingEntity
                    player.attack(it)
                }
            }
        }

        var castCount = 3
        object: BukkitRunnable() {
            override fun run() {
                castSlash()
                castCount -= 1
                if (castCount == 0) cancel()
            }
        }.runTaskTimer(instance, 0L, 6L)
    }

    private fun skillSlide() {
        player.velocity = player.eyeLocation.direction.multiply(2.0)
        player.velocity.setY(0.2)
        val firstLocation = player.location

        Lib.playGlobalSound(player, Sound.ITEM_FIRECHARGE_USE, 4.0, 1.0f)
        Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 4.0, 0.8f)

        object: BukkitRunnable() {
            override fun run() {
                player.getNearbyEntities(2.0, 2.0, 2.0).filter {!it.scoreboardTags.contains("arena_hit_cooldown") && it is LivingEntity}.forEach {
                    it as LivingEntity
                    player.attack(it)
                    it.scoreboardTags.add("arena_hit_cooldown")
                }

                player.world.spawnParticle(Particle.SWEEP_ATTACK, player.location.add(0.0, 0.5, 0.0), 3, 0.0, 0.0, 0.0, 0.0)

                if (firstLocation.distance(player.location) >= 3.0) {
                    cancel()
                    player.world.entities.filter {it.scoreboardTags.contains("arena_hit_cooldown")}.forEach {
                        it.scoreboardTags.remove("arena_hit_cooldown")
                    }
                }
            }
        }.runTaskTimer(instance, 0L, 1L)
    }

    private fun skillRoundSlash() {
        var rotatedDegree = 0

        Lib.playGlobalSound(player, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 4.0, 0.9f)
        Lib.playGlobalSound(player, Sound.BLOCK_ANVIL_LAND, 4.0, 1.2f)

        object: BukkitRunnable() {
            override fun run() {
                rotatedDegree += 24

                // player
                val location = player.location

                location.yaw += 24.0f
                location.pitch = 30.0f

                player.teleport(location)

                // effect
                val effectiveLocation = player.location.add(player.location.direction.setY(0.0).multiply(4.0)).add(0.0, 0.75, 0.0)
                player.world.spawnParticle(Particle.SWEEP_ATTACK, effectiveLocation, 8, 0.5, 0.5, 0.5, 0.0)

                player.getNearbyEntities(10.0, 10.0, 10.0).filter {it.location.distance(effectiveLocation) <= 2.0 && it is Monster }.forEach {
                    it as Monster
                    player.attack(it)
                }

                if (rotatedDegree >= 360) {
                    cancel()
                }
            }
        }.runTaskTimer(instance, 0L, 1L)
    }

    private fun skillBuff() {
        val health = player.health - player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * 0.5

        if (health >= 0.0){
            Lib.playGlobalSound(player, Sound.ENTITY_WOLF_HOWL, 4.0, 0.9f)
            Lib.playGlobalSound(player, Sound.ENTITY_PIGLIN_ANGRY, 4.0, 0.9f)

            player.health -= player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * 0.5

            player.world.spawnParticle(Particle.DAMAGE_INDICATOR, player.eyeLocation, 20, 0.5, 0.5, 0.5, 1.0)

            player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 600, 5))
            player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 60, 0))

            player.world.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, player.location, 10, 0.25, 0.1, 0.25, 0.8)
            player.world.spawnParticle(Particle.FLAME, player.location, 20, 0.5, 0.5, 0.5, 0.1)
        } else {
            player.playSound(player, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.75f)
            player.playSound(player, Sound.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED, 1.0f, 0.75f)
        }
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
