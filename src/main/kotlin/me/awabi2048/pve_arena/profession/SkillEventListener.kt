package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.item.BowItem
import me.awabi2048.pve_arena.item.SwordItem
import me.awabi2048.pve_arena.item.WandItem
import me.awabi2048.pve_arena.item.wand.WandAbility
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

object SkillEventListener : Listener {
    @EventHandler
    fun onPlayerUsedActiveSkill(event: PlayerInteractEvent) {
//        println("called. ${event.action}")
        if (event.action !in listOf(
                Action.LEFT_CLICK_AIR,
                Action.RIGHT_CLICK_AIR,
                Action.LEFT_CLICK_BLOCK,
                Action.RIGHT_CLICK_BLOCK
            )
        ) return
        if (event.hand != EquipmentSlot.HAND) return

        val player = event.player
        val item = event.item ?: return
        val playerProfession = PlayerProfession.getProfession(player) ?: return

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
            when (event.action) {

                in listOf(
                    Action.LEFT_CLICK_AIR,
                    Action.LEFT_CLICK_BLOCK
                ),
                    -> Swordsman(player).spell(ProfessionSkillState.SpellClick.LEFT)

                in listOf(
                    Action.RIGHT_CLICK_AIR,
                    Action.RIGHT_CLICK_BLOCK
                ),
                    -> Swordsman(player).spell(ProfessionSkillState.SpellClick.RIGHT)

                else -> return
            }
        }

        // Archer
        if (
            playerProfession == PlayerProfession.ARCHER &&
            (item.type in listOf(
                Material.BOW
            ) || BowItem.getFromItem(item) in BowItem.list)
        ) {
            when (event.action) {

                in listOf(
                    Action.LEFT_CLICK_AIR,
                    Action.LEFT_CLICK_BLOCK
                ),
                    -> Archer(player).spell(ProfessionSkillState.SpellClick.LEFT)

                in listOf(
                    Action.RIGHT_CLICK_AIR,
                    Action.RIGHT_CLICK_BLOCK
                ),
                    -> Archer(player).spell(ProfessionSkillState.SpellClick.RIGHT)

                else -> return
            }
        }

        // Mage
        if (
            playerProfession == PlayerProfession.MAGE &&
            WandItem.getFromItem(item) in WandItem.list) {

            val wand = WandAbility(event.player, event.item!!)

            if (event.action in listOf(
                    Action.RIGHT_CLICK_AIR,
                    Action.RIGHT_CLICK_BLOCK
                ) && PlayerProfession.getProfession(player) == PlayerProfession.MAGE
            ) Mage(player).spell(
                ProfessionSkillState.SpellClick.RIGHT
            )
            if (event.action in listOf(
                    Action.LEFT_CLICK_AIR,
                    Action.LEFT_CLICK_BLOCK
                )
            ) wand.shoot(player.attackCooldown)
        }
    }
}
