package me.awabi2048.pve_arena.custom_mob.mob_behavior

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.custom_mob.mob_behavior.MobBehavior.*
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

fun Monster.forceBehavior(behavior: MobBehavior) {
    when (behavior) {
        POUNCE -> {
            velocity = Vector(
                eyeLocation.direction.setY(0.0).normalize().multiply(1.0).x,
                0.3,
                eyeLocation.direction.setY(0.0).normalize().multiply(1.0).z,
            )

            attack(target!!)
        }

        ENRAGE -> {
            addPotionEffect(PotionEffect(PotionEffectType.SPEED, 2, 4))
            addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 2, 4))
        }

        SWORD_SLASH -> {
            Lib.playGlobalSound(target!! as Player, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 6.0, 1.2f)

            val attackLocation =
                eyeLocation.clone().add(eyeLocation.direction.setY(0.0).normalize().multiply(0.3)).add(0.0, -0.5, 0.0)
            (target!! as Player).getNearbyEntities(32.0, 32.0, 32.0)
                .filter { it is Player && it.location.distance(attackLocation) <= 2.0 }.forEach {
                (it as Player).damage(getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value, this)
            }
            location.world.spawnParticle(Particle.SWEEP_ATTACK, attackLocation, 4, 0.0, 0.0, 0.0, 0.0)
        }

        ACCELERATE -> {
            velocity = if (!isOnGround) {
                Vector(
                    eyeLocation.direction.setY(0.0).normalize().multiply(2.0).x,
                    -1.0,
                    eyeLocation.direction.setY(0.0).normalize().multiply(2.0).z,
                )
            } else {
                Vector(
                    eyeLocation.direction.setY(0.0).normalize().multiply(2.0).x,
                    1.0,
                    eyeLocation.direction.setY(0.0).normalize().multiply(2.0).z,
                )
            }
        }

        CURSE -> {
            (target!! as Player).spawnParticle(Particle.ELDER_GUARDIAN, target!!.location, 1)
            (target!! as Player).playSound(target!!, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f)

            target!!.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 2, 1))
            target!!.damage(getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value, this)
        }
    }
}

fun Monster.applyBehavior() {
    val behavior = when (type) {
        EntityType.ZOMBIE -> {
            val possible = listOf(ENRAGE)

            if (equipment.itemInMainHand.type.name.contains("SWORD") && location.distance(target!!.location) <= 3.0) possible.plus(
                SWORD_SLASH
            )
            if (location.distance(target!!.location) <= 5.0) possible.plus(POUNCE)

            possible.random()
        }

        EntityType.SKELETON -> {
            val possible = listOf(ENRAGE)

            if (!equipment.itemInMainHand.type.name.contains("BOW") && location.distance(target!!.location) <= 5.0) possible.plus(
                POUNCE
            )

            possible.random()
        }

        EntityType.GUARDIAN -> {
            val possible = listOf(ENRAGE)

            if (equipment.itemInMainHand.type.name.contains("SWORD") && location.distance(target!!.location) <= 3.0) possible.plus(
                SWORD_SLASH
            )
            if (location.distance(target!!.location) <= 5.0) possible.plus(POUNCE)

            possible.random()
        }

        EntityType.BLAZE -> listOf(
            ACCELERATE,
        ).random()

        else -> null
    } ?: return

    forceBehavior(behavior)
}

fun Monster.startBehavior() {
    object : BukkitRunnable() {
        override fun run() {
            if (isDead) {
                cancel()
            } else {
                applyBehavior()
            }
        }
    }.runTaskTimer(instance, 200L, (100..200).random().toLong())
}

