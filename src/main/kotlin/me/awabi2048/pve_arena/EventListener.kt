package me.awabi2048.pve_arena

import com.destroystokyo.paper.event.entity.EndermanEscapeEvent
import io.papermc.paper.event.entity.ElderGuardianAppearanceEvent
import io.papermc.paper.event.entity.EntityMoveEvent
import me.awabi2048.pve_arena.Main.Companion.activeSession
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.lobbyOriginLocation
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.Main.Companion.spawnSessionKillCount
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.game.Generic
import me.awabi2048.pve_arena.game.NormalArena
import me.awabi2048.pve_arena.game.QuickArena
import me.awabi2048.pve_arena.game.WaveProcessingMode
import me.awabi2048.pve_arena.menu_manager.EntranceMenu
import me.awabi2048.pve_arena.menu_manager.MenuManager
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.damage.DamageSource
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.entity.SlimeSplitEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

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
        if (Lib.lookForSession(uuid) is WaveProcessingMode) {
            val difficulty = (Lib.lookForSession(uuid) as Generic.Status.InGame).difficulty
            event.droppedExp *= when (difficulty) {
                WaveProcessingMode.MobDifficulty.EASY -> 1.2.toInt()
                WaveProcessingMode.MobDifficulty.NORMAL -> 1.5.toInt()
                WaveProcessingMode.MobDifficulty.HARD -> 1.75.toInt()
                WaveProcessingMode.MobDifficulty.EXPERT -> 2.0.toInt()
                WaveProcessingMode.MobDifficulty.NIGHTMARE -> 3.0.toInt()
            }
        }
    }

    @EventHandler
    fun playerDamageModify(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return


    }

    @EventHandler
    fun playerTakenDamageModify(event: EntityDamageByEntityEvent) {
        println("Damager is ${event.damager}")

        if (event.entity.world.name.startsWith("arena_session.")) {
            if (event.entity !is Player) return

            if (event.damager is Skeleton || event.damager is Stray || event.damager is Bogged) {
                event.damage = (event.damager as LivingEntity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value
            }

            if (event.damager is Drowned) {
                event.damage = (event.damager as Drowned).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value
            }

            if (event.damager is Guardian) {
                event.damage = (event.damager as Guardian).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value
            }
        }
    }

    @EventHandler
    fun stopSessionMenu(event: InventoryClickEvent) {
        if (event.view.title == "アリーナセッション管理") {
            event.isCancelled = true
            if (event.slot !in 9..35) return
            if (!event.isShiftClick && (event.click.isMouseClick && !event.click.isLeftClick && !event.click.isRightClick)) return
            val uuid = event.currentItem?.itemMeta?.itemName ?: return
            val session = Lib.lookForSession(uuid)!!
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
//                println("SESSION SIZE: ${activeSession.size}")

                val uuid = world.name.substringAfter("arena_session.")
                val session = Lib.lookForSession(uuid)!!
                session.stop()
            }
        }
    }

    @EventHandler
    fun regulateMobTarget(event: EntityTargetLivingEntityEvent) {
        if (event.entity.world.name.startsWith("arena_session.") && event.target !is Player) {
            event.isCancelled = true
            event.target = event.entity.world.players.random()
        }
    }

//    @EventHandler
//    fun regulateMobMovement(event: Entity) {
//        if (event.entity.world.name.startsWith("arena_session.")) {
//            val movedDistance = event.to.distance(event.from)
//
//            if (event.entity is Spider && event.entity.isClimbing) event.isCancelled = true
//            if (event.entity is Blaze && event.entity.isOnGround) event.isCancelled = true
//
//            if ((event.entity is Enderman || event.entity is Shulker) && movedDistance >= 1.0) event.isCancelled = true
//
//        }
//    }

//    @EventHandler
//    fun regulateElderGuardian(event: EntityPotionEffectEvent) {
//        event.cause == EntityPotionEffectEvent.Cause.
//
//        if (!event.entity.world.name.startsWith("arena_session.")) return
//        event.isCancelled = true
//        event.affectedPlayer.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 5, 1))
//        event.affectedPlayer.damage(8.0)
//    }

    @EventHandler
    fun onEntranceMenuClicked(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) return
        if (event.clickedInventory?.any { it != null && it.itemMeta.itemName == "§cゲートを開く" } == true) {
            event.isCancelled = true
            if (event.whoClicked.scoreboardTags.contains("arena.misc.in_click_interval")) return

            val menu = EntranceMenu(event.whoClicked as Player)
            val inverted = event.isRightClick
            val player = event.whoClicked as Player

            if (event.slot in listOf(19, 21, 23, 25, 40)) {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            }

            // on start
            if (event.slot == 40) {
                player.closeInventory()
                player.scoreboardTags.add("arena.misc.in_click_interval")

                player.sendMessage("$prefix §7ゲートを開いています....")

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        player.scoreboardTags.remove("arena.misc.in_click_interval")
                    },
                    100L
                )

                menu.openGate(event.clickedInventory!!)

            } else {

                // precession
                when (event.slot) {
                    19 -> menu.cycleOption(event.inventory, EntranceMenu.OptionCategory.MobType, inverted)
                    21 -> menu.cycleOption(event.inventory, EntranceMenu.OptionCategory.MobDifficulty, inverted)
                    23 -> menu.changeOptionValue(event.inventory, EntranceMenu.OptionCategory.PlayerCount, inverted)
                    25 -> menu.changeOptionValue(event.inventory, EntranceMenu.OptionCategory.SacrificeAmount, inverted)
                }

                //
                player.scoreboardTags.add("arena.misc.in_click_interval")
                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        player.scoreboardTags.remove("arena.misc.in_click_interval")
                    },
                    3L
                )
            }
        }
    }

    @EventHandler
    fun onMenuOpen(event: PlayerInteractAtEntityEvent) {
        if (!event.rightClicked.scoreboardTags.contains("arena.interaction")) return
        event.isCancelled = true

        if (event.rightClicked.scoreboardTags.contains("arena.misc.game.entrance_menu") && !event.player.scoreboardTags.contains(
                "arena.misc.in_click_interval"
            )
        ) {
            val menu = EntranceMenu(event.player)
            menu.open()
        }
    }

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

//    @EventHandler
//    fun onQuestBoardOpen(event: PlayerInteractEntityEvent) {
//        if (!event.rightClicked.scoreboardTags.contains("arena.interaction.quest_board")) return
//
//        val menu = MenuManager(event.player, MenuManager.MenuType.Quest())
//    }
}
