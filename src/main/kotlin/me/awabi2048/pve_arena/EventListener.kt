package me.awabi2048.pve_arena

import me.awabi2048.pve_arena.Main.Companion.activeParty
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.lobbyOriginLocation
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.Main.Companion.spawnSessionKillCount
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.game.*
import me.awabi2048.pve_arena.item.AccessoryItem
import me.awabi2048.pve_arena.item.ItemManager
import me.awabi2048.pve_arena.item.SacrificeItem
import me.awabi2048.pve_arena.misc.Lib
import me.awabi2048.pve_arena.party.ChatChannel
import me.awabi2048.pve_arena.party.Party
import me.awabi2048.pve_arena.party.Party.Companion.playerChatListening
import me.awabi2048.pve_arena.party.Party.Companion.playerChatState
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.entity.SlimeSplitEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.roundToInt

object EventListener : Listener {


    @EventHandler
    fun onPlayerKillInArena(event: EntityDeathEvent) {
        if (event.entity.world.name.startsWith("arena_session")) {
            val uuid = event.entity.world.name.substringAfter("arena_session.")
            val session = Lib.lookForSession(uuid)!!
            if (session.status !is Generic.Status.InGame) return

            if (event.entity.world.entities.none { it.scoreboardTags.contains("arena.mob") } && session is WaveProcessingMode) {
                val lastWave = when (session) {
                    is NormalArena -> session.lastWave
                    is QuickArena -> DataFile.config.getInt("misc.game.quick_arena", 10)
                    else -> 1
                }

                if (session is QuickArena && (session.status as Generic.Status.InGame).wave != lastWave) return

                // ウェーブ終了処理
                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        session.finishWave(
                            event.entity.world,
                            (session.status as Generic.Status.InGame).wave,
                            lastWave,
                            (session.status as Generic.Status.InGame).timeElapsed
                        )

                        if ((session.status as Generic.Status.InGame).wave == lastWave) Lib.lookForSession(uuid)!!.status =
                            Generic.Status.WaitingFinish
                    },
                    40L
                )
            }

            // scoreboard
            if (session is QuickArena) return

            val totalMobCount = when (session) {
                is NormalArena -> session.summonCountCalc((session.status as Generic.Status.InGame).wave)
                else -> 0
            }

            event.entity.world.players.forEach { it ->
                val displayScoreboard = Main.displayScoreboardMap[it]!!
                val currentMobCount =
                    event.entity.world.entities.filter { it.scoreboardTags.contains("arena.mob") }.size

                if (currentMobCount == totalMobCount - 1) displayScoreboard.scoreboard!!.resetScores("§fMobs §c$totalMobCount§7/$totalMobCount")
                spawnSessionKillCount[uuid] = spawnSessionKillCount[uuid]!! + 1

                for (count in 0..totalMobCount) {
                    displayScoreboard.scoreboard!!.resetScores("§fMobs §c${count}§7/$totalMobCount")
                }

                displayScoreboard.getScore("§fMobs §c${totalMobCount - spawnSessionKillCount[uuid]!!}§7/$totalMobCount").score =
                    1
                if (spawnSessionKillCount[uuid]!! == totalMobCount) spawnSessionKillCount.remove(uuid)
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
            val session = Lib.lookForSession(uuid)!!
            if (!session.players.contains(event.player)) {
                event.player.teleport(lobbyOriginLocation)
                event.player.sendMessage("$prefix §c§lWARNING §cあなたは入場を許可されていません。")
            }
        }

        for (world in Bukkit.getWorlds().filter { it.name.startsWith("arena_session.") }) {
            if (world.players.isEmpty()) {
                val uuid = world.name.substringAfter("arena_session.")
                val session = Lib.lookForSession(uuid)!!
                session.stop()
            }
        }
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        if (event.player.world.name.startsWith("arena_session.")) {
            event.player.teleport(lobbyOriginLocation)
        }
    }

    @EventHandler
    fun regulateMobTarget(event: EntityTargetLivingEntityEvent) {
        if (event.entity.world.name.startsWith("arena_session.") && event.target !is Player && event.entity.world.players.isNotEmpty()) {
            event.isCancelled = true
            event.target = event.entity.world.players.random()
        }
    }

    @EventHandler
    fun onSneak(event: PlayerToggleSneakEvent) {
        val player = event.player
        if (SacrificeItem.getFromItem(player.equipment.itemInMainHand) == ItemManager.ArenaItem.SACRIFICE_ITEM) {
            // チャージ開始
            if (event.isSneaking) {
                object : BukkitRunnable() {
                    override fun run() {
                        if (!player.isSneaking) cancel()
                        if (player.level < 3) cancel()
                        if (SacrificeItem.getFromItem(player.equipment.itemInMainHand) != ItemManager.ArenaItem.SACRIFICE_ITEM) {
                            cancel()
                            return
                        }

                        player.level -= 3
                        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 0.6f)
                        val chargedItem = SacrificeItem.charge(player.equipment.itemInMainHand)
                        player.equipment.setItemInMainHand(chargedItem)
                    }
                }.runTaskTimer(instance, 5, 5)
            }
        }
    }

    @EventHandler
    fun onPlayerSendMessage(event: PlayerChatEvent) {
        if (playerChatState[event.player] == ChatChannel.PARTY && playerChatListening[event.player] == null) {
            val party = activeParty.find { it.players.contains(event.player) }!!
            party.sendPartyMessage(event.player, event.message)

            event.isCancelled = true
        }
    }
}
