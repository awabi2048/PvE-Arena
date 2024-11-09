package me.awabi2048.kota_arena

import io.papermc.paper.event.entity.EntityMoveEvent
import io.papermc.paper.event.player.PlayerArmSwingEvent
import me.awabi2048.kota_arena.Main.Companion.playerMenuStatus
import me.awabi2048.kota_arena.menu_manager.entrance.entranceMenuClicked
import me.awabi2048.kota_arena.menu_manager.entrance.getEntranceMenu
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerAnimationType
import org.bukkit.event.player.PlayerInteractEntityEvent

object EventListener: Listener {
    @EventHandler
    fun onPlayerAttack(event: EntityDamageByEntityEvent) {
        if (event.damager.type == EntityType.PLAYER) return
        event.damager.sendMessage("you hurt!")
        Bukkit.broadcastMessage("hurt")
    }

    @EventHandler
    fun onEntranceMenuOpened(event: PlayerInteractEntityEvent) {
        if (!event.rightClicked.scoreboardTags.contains("arena.normal.entrance_interaction")) return

        val player = event.player

        // メニュー開く
        val entranceMenu = getEntranceMenu(event.player, event.rightClicked.uniqueId.toString())?: return
        player.openInventory(entranceMenu)

        // 効果音
        player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f)

        // パーティクル
        player.world.spawnParticle(Particle.WITCH, event.rightClicked.location.toCenterLocation(), 50, 0.5, 0.5, 0.5)

        // フラグ設定
        playerMenuStatus[player] = "entrance"

        event.isCancelled = true

    }

    @EventHandler
    fun onEntranceMenuClicked(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) return
        if (playerMenuStatus[event.whoClicked] != "entrance") return

        if (event.slot !in listOf(19, 21, 23, 25, 40)) return

        entranceMenuClicked(event)
        event.isCancelled = true

    }

    fun onCloseInventory(event: InventoryCloseEvent) {
        playerMenuStatus.remove(event.player)
    }

}
