package me.awabi2048.pve_arena

import me.awabi2048.pve_arena.Main.Companion.activeSession
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.lobbyOriginLocation
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.Main.Companion.spawnSessionKillCount
import me.awabi2048.pve_arena.game.Generic
import me.awabi2048.pve_arena.game.NormalArena
import me.awabi2048.pve_arena.game.QuickArena
import me.awabi2048.pve_arena.game.WaveProcessingMode
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.SlimeSplitEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerChangedWorldEvent

object EventListener : Listener {
    @EventHandler
    fun slimeSplit(event: SlimeSplitEvent) {
        if (!event.entity.location.world.toString().startsWith("arena_session")) return
        event.isCancelled = true
    }

    @EventHandler
    fun expModify(event: EntityDeathEvent) {
        if (!event.entity.location.world.toString().startsWith("arena_session")) return

        val uuid = event.entity.location.world.toString().substringAfter("arena_session.")
        val difficulty = (Lib.lookForSession(uuid) as Generic.Status.InGame).difficulty
        event.droppedExp *= when (difficulty) {
            WaveProcessingMode.MobDifficulty.EASY -> 1.2.toInt()
            WaveProcessingMode.MobDifficulty.NORMAL -> 1.5.toInt()
            WaveProcessingMode.MobDifficulty.HARD -> 1.75.toInt()
            WaveProcessingMode.MobDifficulty.EXPERT -> 2.0.toInt()
            WaveProcessingMode.MobDifficulty.NIGHTMARE -> 3.0.toInt()
        }
    }

    @EventHandler
    fun onPlayerAttack(event: EntityDamageByEntityEvent) {
        if (event.damager.type == EntityType.PLAYER) return
        event.damager.sendMessage("you hurt!")
        Bukkit.broadcastMessage("hurt")
    }

    @EventHandler
    fun stopSessionMenu(event: InventoryClickEvent) {
        if (event.view.title == "アリーナセッション管理") {
            event.isCancelled = true
            if (event.slot !in 9..35) return
            if (!event.isShiftClick && (event.click.isMouseClick && !event.click.isLeftClick && !event.click.isRightClick)) return
            val uuid = event.currentItem?.itemMeta?.itemName?: return
            val session = Lib.lookForSession(uuid)
            session.stop()
            event.whoClicked.sendMessage("$prefix §eUUID: $uuid のセッションを強制終了しました。")
        }
    }

//    @EventHandler
//    fun onEntranceMenuOpened(event: PlayerInteractEntityEvent) {
//        if (!event.rightClicked.scoreboardTags.contains("arena.normal.entrance_interaction")) return
//        if (event.rightClicked.scoreboardTags.contains("arena.normal.portal_open")) return
//
//        val player = event.player
//
//        // メニュー開く
//        val entranceMenu = getEntranceMenu(event.player, event.rightClicked.uniqueId.toString()) ?: return
//        player.openInventory(entranceMenu)
//
//        // 効果音
//        player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f)
//
//        // パーティクル
//        player.world.spawnParticle(Particle.WITCH, event.rightClicked.location.toCenterLocation(), 50, 0.5, 0.5, 0.5)
//
//        // フラグ設定
//        playerMenuStatus[player] = "entrance"
//
//        event.isCancelled = true
//
//    }

    @EventHandler
    fun onPlayerKillInArena(event: EntityDeathEvent) {
        if (event.entity.world.name.startsWith("arena_session") && event.entity.killer is Player) {
            val uuid = event.entity.world.name.substringAfter("arena_session.")
            val session = Lib.lookForSession(uuid)

            if (event.entity.world.entities.none { it.scoreboardTags.contains("arena.mob") } && session is WaveProcessingMode) {
                val lastWave = when(session) {
                    is NormalArena -> session.lastWave
                    is QuickArena -> 10
                    else -> 1
                }

                // ウェーブ終了処理
                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        session.finishWave(
                            world = event.entity.world,
                            wave = (session.status as Generic.Status.InGame).wave,
                            lastWave = lastWave
                        )
                    },
                    40L
                )
            }

            // scoreboard
            val totalMobCount = when(session) {
                is NormalArena -> session.summonCountCalc((session.status as Generic.Status.InGame).wave)
                is QuickArena -> 12
                else -> 0
            }

            event.entity.world.players.forEach{ it ->
                val displayScoreboard = Main.displayScoreboardMap[it]!!
                val currentMobCount = event.entity.world.entities.filter{it.scoreboardTags.contains("arena.mob")}.size

                if (currentMobCount == totalMobCount - 1) displayScoreboard.scoreboard!!.resetScores("§fMobs §c$totalMobCount§7/$totalMobCount")
                spawnSessionKillCount[uuid] = spawnSessionKillCount[uuid]!! + 1

                for (count in 0..totalMobCount) {
                    displayScoreboard.scoreboard!!.resetScores("§fMobs §c$${totalMobCount}§7/$totalMobCount")
                }

                displayScoreboard.getScore("§fMobs §c${totalMobCount  - spawnSessionKillCount[uuid]!!}§7/$totalMobCount").score = 1
                if  (spawnSessionKillCount[uuid]!! == 0)  spawnSessionKillCount.remove(uuid)
            }
        }
    }

    @EventHandler
    fun onPlayerMoveDimension(event: PlayerChangedWorldEvent) {

        if (Main.displayScoreboardMap[event.player] != null && !event.player.world.name.startsWith("arena_session.")) {
            // reset scoreboard
            val scoreboard = Main.displayScoreboardMap[event.player]!!
            scoreboard.unregister()
        }

        if (event.player.world.name.startsWith("arena_session.")) {
            val uuid = event.player.world.name.substringAfter("arena_session.")
            val session = Lib.lookForSession(uuid)
            if (!session.players.contains(event.player)) {
                event.player.teleport(lobbyOriginLocation)
                event.player.sendMessage("$prefix §c§lWARNING: §cあなたは入場を許可されていません。")
            }
        }
    }

//    @EventHandler
//    fun onEntranceMenuClicked(event: InventoryClickEvent) {
//        if (event.whoClicked !is Player) return
//        if (playerMenuStatus[event.whoClicked] != "entrance") return
//
//        if (event.slot !in listOf(19, 21, 23, 25, 40)) return
//
//        entranceMenuClicked(event)
//        event.isCancelled = true
//
//    }
//
//    @EventHandler
//    fun onCloseInventory(event: InventoryCloseEvent) {
//        playerMenuStatus.remove(event.player)
//    }

//    @EventHandler
//    fun onPortalEnter(event: PlayerMoveEvent) {
//        val player = event.player
//
//        val playerX = player.location.toCenterLocation().x
//        val playerY = player.location.toCenterLocation().y
//        val playerZ = player.location.toCenterLocation().z
//
//        for (entranceCollider in player.location.getNearbyPlayers(3.0)) {
//            val colliderX = entranceCollider.location.toCenterLocation().x
//            val colliderY = entranceCollider.location.toCenterLocation().y
//            val colliderZ = entranceCollider.location.toCenterLocation().z
//
//            if (
//                (playerX in colliderX - 1..colliderX + 1)
//                && (playerY in colliderY - 1..colliderY + 1)
//                && (playerZ in colliderZ - 1..colliderZ + 1)
//            ) {
//                val a = EntrancePortal()
//            }
//        }
//    }

//    @EventHandler
//    fun onPortalEnter(event: PlayerInteractEntityEvent) {
//        if (!event.rightClicked.scoreboardTags.contains("arena.normal.portal_open")) return
//            for (session in sessionDataMap.values) {
//                if (event.rightClicked == session.portalEntity) {
//
//                    val uuid = sessionDataMap.filterKeys { sessionDataMap[it] == session }.keys.toList()[0]
//
//                    val portal = EntrancePortal(uuid).transportPlayer(event.player)
//                }
//            }
//    }
}
