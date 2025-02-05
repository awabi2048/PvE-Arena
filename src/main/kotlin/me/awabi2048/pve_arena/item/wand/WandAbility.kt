package me.awabi2048.pve_arena.item.wand

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.profession.Mage
import me.awabi2048.pve_arena.profession.PlayerProfession
import me.awabi2048.pve_arena.profession.Profession
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.pow

class WandAbility(val player: Player, val item: ItemStack) {
    fun shoot(charge: Float) {
        if (player.scoreboardTags.contains("arena.misc.wand_cooldown")) return
        player.scoreboardTags.add("arena.misc.wand_cooldown")
        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                player.scoreboardTags.remove("arena.misc.wand_cooldown")
            },
            7
        )

        val range = 8.0 * charge

        val damage =
            player.equipment.itemInMainHand.itemMeta.getAttributeModifiers(
                Attribute.GENERIC_ATTACK_DAMAGE
            )!!.toList()[0].amount * charge

        val directionVec = player.eyeLocation.direction.multiply(0.33)
        val location = player.eyeLocation
        location.y -= 0.2

        val damagedEntity: MutableList<Monster> = mutableListOf()

        while (location.distance(player.eyeLocation) < range) {
            location.world.spawnParticle(Particle.CRIT, location, 1, 0.0, 0.0, 0.0, 0.1)
            location.add(directionVec)

            damagedEntity += player.getNearbyEntities(16.0, 16.0, 16.0).filterIsInstance<Monster>()
                .filter { it.boundingBox.contains(location.toVector()) && !damagedEntity.contains(it)}
        }

        damagedEntity.forEach {
            val decayedDamage = damage * it.location.distance(player.location).pow(-0.1)

            it.damage(decayedDamage, player)
            val event = EntityDamageByEntityEvent(
                player,
                it,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                decayedDamage
            )

            Bukkit.getPluginManager().callEvent(event)
//                it.scoreboardTags.add("arena.misc.hit_cooldown")
//                Bukkit.getScheduler().runTaskLater(
//                    instance,
//                    Runnable {
//                        it.scoreboardTags.remove("arena.misc.hit_cooldown")
//                    },
//                    5L
//                )
        }

        // sound
        location.world.players.filter { it.location.distance(location) <= 10 }.forEach {
                it.playSound(it, Sound.ENTITY_ZOMBIE_INFECT, 1.0f, ((12..18).random() / 10).toFloat())
        }

        // spell
//        if (PlayerProfession.getProfession(player) == PlayerProfession.MAGE) Mage(player).spell(ClickType.LEFT)
    }
}
