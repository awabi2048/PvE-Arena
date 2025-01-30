package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.profession.PlayerProfession.*
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable

abstract class Profession(private val owner: Player) {
    fun spell(click: ClickType) {
        if (!owner.hasMetadata("arena_ability_spell")) {
            owner.setMetadata("arena_ability_spell", FixedMetadataValue(instance, listOf((click))))
        } else {
            val currentSpell = owner.getMetadata("arena_ability_spell")[0].value() as List<ClickType>
            owner.setMetadata("arena_ability_spell", FixedMetadataValue(instance, currentSpell + click))
        }

        owner.setMetadata("arena_ability_spell_called", FixedMetadataValue(instance, System.currentTimeMillis()))

        // spell completed
        if ((owner.getMetadata("arena_ability_spell")[0].value() as List<*>).size == 3) {
            val spell = owner.getMetadata("arena_ability_spell")[0].value() as List<ClickType>
            owner.removeMetadata("arena_ability_spell", instance)

            val profession = PlayerProfession.getProfession(owner)

            when(profession) {
                MAGE -> Mage(owner).callSkill(spell)
                ARCHER -> Archer(owner).callSkill(spell)
                SWORDSMAN -> Swordsman(owner).callSkill(spell)
                null -> return
            }
        }

        val ifChecking = owner.getMetadata("arena_ability_spell_checking")

        // expire
        if (ifChecking.isEmpty()){
            owner.setMetadata(
                "arena_ability_spell_checking",
                FixedMetadataValue(instance, true)
            )

            object : BukkitRunnable() {
                override fun run() {
                    if (System.currentTimeMillis() - owner.getMetadata("arena_ability_spell_called")[0].value() as Long >= 750) {
                        owner.removeMetadata(
                            "arena_ability_spell",
                            instance
                        )
                        owner.removeMetadata("arena_ability_spell_checking", instance)
                        owner.playSound(owner, Sound.UI_BUTTON_CLICK, 1.0f, 0.5f)
                        cancel()
                        return
                    }
                }
            }.runTaskTimer(instance, 1, 1)
        }
    }


}
