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
            10L
        )

        val range =
            if (player.scoreboardTags.contains("arena.misc.wand_charged")) {
                16.0 * charge
            } else {
                8.0 * charge
            }

        val damage =
            player.equipment.itemInMainHand.itemMeta.getAttributeModifiers(
                Attribute.GENERIC_ATTACK_DAMAGE
            )!!.toList()[0].amount * charge

        val directionVec = player.eyeLocation.direction.multiply(0.33)
        val location = player.eyeLocation
        location.y -= 0.2

        while (location.distance(player.eyeLocation) < range) {
            location.world.spawnParticle(Particle.CRIT, location, 1, 0.0, 0.0, 0.0, 0.1)
            location.add(directionVec)

            val decayedDamage = damage * location.distance(player.eyeLocation).pow(-0.1)

            player.getNearbyEntities(16.0, 16.0, 16.0)
                .filter { it.boundingBox.contains(location.toVector()) && it is Monster }.forEach {
                    (it as LivingEntity)

                    it.damage(decayedDamage)
                    val event = EntityDamageByEntityEvent(
                        player,
                        it,
                        EntityDamageEvent.DamageCause.MAGIC,
                        decayedDamage
                    )
                    Bukkit.getPluginManager().callEvent(event)
                }
        }

        // sound
        location.world.players.filter { it.location.distance(location) <= 10 }.forEach {
            it.playSound(it, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, ((12..18).random() / 10).toFloat())
        }

        // spell
        spell(ClickType.LEFT)
//        if (Mage(player).getProfession() == PlayerProfession.MAGE) spell(ClickType.LEFT)
    }

    fun charge() {
        spell(ClickType.RIGHT)
//        if (Mage(player).getProfession() == PlayerProfession.MAGE) spell(ClickType.RIGHT)
    }

    private fun spell(click: ClickType) {
        if (!player.hasMetadata("arena_ability_spell")) {
            player.setMetadata("arena_ability_spell", FixedMetadataValue(instance, listOf((click))))
        } else {
            val currentSpell = player.getMetadata("arena_ability_spell")[0].value() as List<ClickType>
            player.setMetadata("arena_ability_spell", FixedMetadataValue(instance, currentSpell + click))
        }

        println(player.getMetadata("arena_ability_spell")[0].value().toString())
        player.sendMessage(player.getMetadata("arena_ability_spell")[0].value().toString())
        player.setMetadata("arena_ability_spell_called", FixedMetadataValue(instance, System.currentTimeMillis()))

        // spelled
        if ((player.getMetadata("arena_ability_spell")[0].value() as List<*>).size == 3) {
            val spell = player.getMetadata("arena_ability_spell")[0].value() as List<ClickType>
            player.removeMetadata("arena_ability_spell", instance)

            Mage(player).callSkill(spell)
        }

        println("${player.getMetadata("arena_ability_spell_checking")[0].value()}")

        // expire
        if (player.getMetadata("arena_ability_spell_checking")[0].value() != true){
            player.setMetadata(
                "arena_ability_spell_checking",
                FixedMetadataValue(instance, true)
            )

            object : BukkitRunnable() {
                override fun run() {
                    if (System.currentTimeMillis() - player.getMetadata("arena_ability_spell_called")[0].value() as Long >= 750) {
                        player.removeMetadata(
                            "arena_ability_spell",
                            instance
                        )
                        player.removeMetadata("arena_ability_spell_checking", instance)
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 0.5f)
                        player.sendMessage("STOP.")
                        cancel()
                        return
                    }
                }
            }.runTaskTimer(instance, 1, 1)
        }
    }
}
