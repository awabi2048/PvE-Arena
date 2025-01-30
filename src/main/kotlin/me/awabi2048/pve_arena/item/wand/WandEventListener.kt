package me.awabi2048.pve_arena.item.wand

import me.awabi2048.pve_arena.item.WandItem
import me.awabi2048.pve_arena.profession.Mage
import me.awabi2048.pve_arena.profession.PlayerProfession
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

object WandEventListener: Listener {
    @EventHandler
    fun onWandClick(event: PlayerInteractEvent) {
        if (event.item == null) return
        if (WandItem.getFromItem(event.item!!) !in WandItem.list) return

        event.isCancelled = true

        val wand = WandAbility(event.player, event.item!!)
        val player = event.player

//        if (event.action in listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK) && PlayerProfession.getProfession(player) == PlayerProfession.MAGE) Mage(player).spell(ClickType.RIGHT)
        if (event.action in listOf(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK)) wand.shoot(player.attackCooldown)
    }

    @EventHandler
    fun onWandClickEntity(event: PlayerInteractEntityEvent) {

        val item = event.player.equipment.itemInMainHand
        if (WandItem.getFromItem(item) !in WandItem.list) return

        event.isCancelled = true
    }

    @EventHandler
    fun onWandLeftClickEntity(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        if (event.cause != DamageCause.ENTITY_ATTACK) return

        val item = (event.damager as Player).equipment.itemInMainHand
        if (WandItem.getFromItem(item) !in WandItem.list) return

        event.isCancelled = true
    }
}
