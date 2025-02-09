package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.item.BowItem
import me.awabi2048.pve_arena.item.SwordItem
import me.awabi2048.pve_arena.item.WandItem
import me.awabi2048.pve_arena.item.wand.WandAbility
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

object SkillEventListener : Listener {
    @EventHandler
    fun onPlayerUsedActiveSkill(event: PlayerInteractEvent) {
//        println("called. ${event.action}")
        if (event.action !in listOf(
                Action.LEFT_CLICK_BLOCK,
                Action.RIGHT_CLICK_BLOCK,
                Action.LEFT_CLICK_AIR,
                Action.RIGHT_CLICK_AIR
            )
        ) return
        if (event.hand != EquipmentSlot.HAND) return

        val click = when(event.action) {
            in listOf(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK, ) -> ProfessionSkillState.SpellClick.LEFT
            in listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK, ) -> ProfessionSkillState.SpellClick.RIGHT
            else -> null
        }!!

        val player = event.player
        val item = event.item ?: return
        val playerProfession = PlayerProfession.getProfession(player) ?: return

        if (player.scoreboardTags.contains("arena_spell_cooldown")) return

        player.scoreboardTags.add("arena_spell_cooldown")
        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                player.scoreboardTags.remove("arena_spell_cooldown")
            },
            2L
        )

        // Swordsman
        if (
            playerProfession == PlayerProfession.SWORDSMAN &&
            (item.type in listOf(

                Material.WOODEN_SWORD,
                Material.STONE_SWORD,
                Material.IRON_SWORD,
                Material.GOLDEN_SWORD,
                Material.DIAMOND_SWORD,
                Material.NETHERITE_SWORD

            ) || SwordItem.getFromItem(item) in SwordItem.list)
        ) {
            when (click) {
                ProfessionSkillState.SpellClick.LEFT -> Swordsman(player).spell(ProfessionSkillState.SpellClick.LEFT)
                ProfessionSkillState.SpellClick.RIGHT -> Swordsman(player).spell(ProfessionSkillState.SpellClick.RIGHT)
            }
        }

        // Archer
        if (
            playerProfession == PlayerProfession.ARCHER &&
            (item.type in listOf(
                Material.BOW
            ) || BowItem.getFromItem(item) in BowItem.list)
        ) {
            when (click) {
                ProfessionSkillState.SpellClick.LEFT -> Archer(player).spell(ProfessionSkillState.SpellClick.LEFT)
                ProfessionSkillState.SpellClick.RIGHT -> Archer(player).spell(ProfessionSkillState.SpellClick.RIGHT)
            }
        }

        // Mage
        if (
            playerProfession == PlayerProfession.MAGE &&
            WandItem.getFromItem(item) in WandItem.list
        ) {

            val wand = WandAbility(event.player, event.item!!)

            if (click == ProfessionSkillState.SpellClick.RIGHT && PlayerProfession.getProfession(player) == PlayerProfession.MAGE) Mage(
                player
            ).spell(ProfessionSkillState.SpellClick.RIGHT)
            if (click == ProfessionSkillState.SpellClick.LEFT && Main.playerSkillState[player] != null) Mage(player).spell(
                ProfessionSkillState.SpellClick.LEFT
            )
        }
    }

    @EventHandler
    fun onArrowHit(event: ProjectileHitEvent) {
        if (event.entity is AbstractArrow) {
            if (event.hitEntity is Enemy && event.entity.shooter is Player) {
                (event.hitEntity as LivingEntity).damage((event.entity as AbstractArrow).damage)
            }

            if (event.entity.scoreboardTags.contains("explosive_arrow")) {
                val entity = event.entity
                entity.location.world.createExplosion(entity.location, 3.0f, false, false)

                entity.remove()
            }
        }
    }
}
