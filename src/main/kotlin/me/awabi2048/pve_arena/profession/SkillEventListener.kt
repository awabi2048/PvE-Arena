package me.awabi2048.pve_arena.profession

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

object SkillEventListener : Listener {
    @EventHandler
    fun onPlayerUsedActiveSkill(event: PlayerInteractEvent) {
        if (event.action !in listOf(
                Action.LEFT_CLICK_AIR,
                Action.RIGHT_CLICK_AIR,
                Action.LEFT_CLICK_BLOCK,
                Action.RIGHT_CLICK_BLOCK
            )
        ) return
        val player = event.player
        val item = event.item ?: return
        val playerProfession = PlayerProfession.getProfession(player) ?: return

        if (playerProfession == PlayerProfession.SWORDSMAN && (item.type in listOf(
                Material.WOODEN_SWORD,
                Material.STONE_SWORD,
                Material.IRON_SWORD,
                Material.GOLDEN_SWORD,
                Material.DIAMOND_SWORD,
                Material.NETHERITE_SWORD
            ) || SwordItem. ))
    }
}
