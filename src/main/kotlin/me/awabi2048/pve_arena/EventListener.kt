package me.awabi2048.pve_arena

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
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.roundToInt

object EventListener : Listener {
    @EventHandler
    fun preventSlimeSplit(event: SlimeSplitEvent) {
        if (!event.entity.location.world.name.startsWith("arena_session")) return
        event.isCancelled = true
    }

    @EventHandler
    fun dropModify(event: EntityDeathEvent) {
        if (!event.entity.location.world.name.startsWith("arena_session") || event.entity !is Monster) return
        val uuid = event.entity.location.world.name.substringAfter("arena_session.")

        if (Lib.lookForSession(uuid) is WaveProcessingMode) {
            val difficulty = (Lib.lookForSession(uuid)?.status as Generic.Status.InGame).difficulty
            val difficultyBonus = DataFile.mobDifficulty.getDouble("${difficulty.toString().substringAfter("MobDifficulty.").lowercase()}.reward_multiplier") * (50..100).random() / 100

            // exp
            event.droppedExp = (event.droppedExp * difficultyBonus * 0.5).roundToInt()
//            println("${event.droppedExp}")

            // drop
            val majorMaterial = listOf(
                Material.ROTTEN_FLESH,
                Material.BONE,
                Material.BLAZE_ROD,
                Material.STRING,
                Material.SPIDER_EYE,
                Material.PRISMARINE_SHARD,
                Material.PRISMARINE_CRYSTALS,
                Material.ENDER_PEARL,
            )

            event.drops.filter {it.type in majorMaterial}.forEach {
                it.amount = (it.amount * difficultyBonus).roundToInt()
//                println("dropped ${it.type} for ${it.amount}")
            }
        }
    }

    @EventHandler
    fun playerDamageModify(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        if (!event.entity.location.world.name.startsWith("arena_session")) return

        val player = event.damager as Player
        if (AccessoryItem.searchPlayerInventory(player, ItemManager.ArenaItem.HUNTER_ACCESSORY) > 0) {
            event.damage *= 1.3
            Lib.healPlayer(player, event.damage * 0.1)
        }

//        if ((event.entity as LivingEntity).health <= event.damage) {
//            val uuid = event.entity.world.name.substringAfter("arena_session.")
//            val session = Lib.lookForSession(uuid)!!
//            if (session.status !is Generic.Status.InGame) return
//
//            if (event.entity.world.entities.filter { it.scoreboardTags.contains("arena.mob") }.size == 1 && session is WaveProcessingMode) {
//                val lastWave = when (session) {
//                    is NormalArena -> session.lastWave
//                    is QuickArena -> DataFile.config.getInt("misc.game.quick_arena", 10)
//                    else -> 1
//                }
//
//                if (session is QuickArena && (session.status as Generic.Status.InGame).wave != lastWave) return
//
//                // ウェーブ終了処理
//                Bukkit.getScheduler().runTaskLater(
//                    instance,
//                    Runnable {
//                        session.finishWave(
//                            event.entity.world,
//                            (session.status as Generic.Status.InGame).wave,
//                            lastWave,
//                            (session.status as Generic.Status.InGame).timeElapsed
//                        )
//
//                        if ((session.status as Generic.Status.InGame).wave == lastWave) Lib.lookForSession(uuid)!!.status =
//                            Generic.Status.WaitingFinish
//                    },
//                    40L
//                )
//            }
//
//            // scoreboard
//            if (session is QuickArena) return
//
//            val totalMobCount = when (session) {
//                is NormalArena -> session.summonCountCalc((session.status as Generic.Status.InGame).wave)
//                else -> 0
//            }
//
//            event.entity.world.players.forEach { it ->
//                val displayScoreboard = Main.displayScoreboardMap[it]!!
//                val currentMobCount =
//                    event.entity.world.entities.filter { it.scoreboardTags.contains("arena.mob") }.size
//
//                if (currentMobCount == totalMobCount - 1) displayScoreboard.scoreboard!!.resetScores("§fMobs §c$totalMobCount§7/$totalMobCount")
//                spawnSessionKillCount[uuid] = spawnSessionKillCount[uuid]!! + 1
//
//                for (count in 0..totalMobCount) {
//                    displayScoreboard.scoreboard!!.resetScores("§fMobs §c${count}§7/$totalMobCount")
//                }
//
//                displayScoreboard.getScore("§fMobs §c${totalMobCount - spawnSessionKillCount[uuid]!!}§7/$totalMobCount").score =
//                    1
//                if (spawnSessionKillCount[uuid]!! == totalMobCount) spawnSessionKillCount.remove(uuid)
//            }
//        }
    }

    @EventHandler
    fun playerTakenDamageModify(event: EntityDamageByEntityEvent) {
        if (event.entity.world.name.startsWith("arena_session.")) {
            if (event.entity !is Player) return

            if (event.damager is AbstractArrow) {
                event.damage = (event.damager as AbstractArrow).damage
            }

            if (event.damager is Trident) {
                event.damage = (event.damager as Trident).damage
            }

            if (event.damager is Guardian) {
                event.damage = (event.damager as Guardian).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value
            }
        }
    }

    @EventHandler
    fun setProjectileDamage(event: ProjectileLaunchEvent) {
        if (event.entity.world.name.startsWith("arena_session.")) {
            if (event.entity is Trident) {
                val shooter = (event.entity as Trident).shooter
                if (shooter is Drowned) (event.entity as Trident).damage =
                    shooter.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value * 0.6
            }

            if (event.entity is AbstractArrow) {
                val shooter = (event.entity as AbstractArrow).shooter
                val entity = event.entity as AbstractArrow
                if (shooter is Skeleton) entity.damage =
                    shooter.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value * 0.6
                if (shooter is Stray) entity.damage =
                    shooter.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value * 0.6
                if (shooter is Bogged) entity.damage =
                    shooter.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value * 0.6
            }

            if (event.entity is SmallFireball) {
                val shooter = (event.entity as SmallFireball).shooter
                val entity = event.entity as SmallFireball
                if (shooter is Blaze) {
                    entity.displayItem = ItemStack(Material.STONE)
                    entity.setMetadata(
                        "damage",
                        FixedMetadataValue(
                            instance,
                            shooter.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value * 0.5
                        )
                    )
                }
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

    @EventHandler
    fun preventEntityFriendlyFire(event: EntityDamageByEntityEvent) {
        if (!event.entity.location.world.name.startsWith("arena_session")) return
        if (event.entity !is Player && event.damager !is Player) {
            // 飛び道具 → プレイヤーからの発射でなければキャンセル
            if (event.damager is Projectile) {
                if ((event.damager as Projectile).shooter !is Player) event.isCancelled = true
            } else { // それ以外
                event.isCancelled = true
            }
        }
    }

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
    fun onAA(event: PlayerMoveEvent) {
        if (event.player.world.name.startsWith("arena_session.")) {
            val uuid = Lib.getUUID(event.player.world)
            val session = Lib.lookForSession(uuid)
            if (session is ArenaDungeon) {
                for (entranceEntity in event.player.world.entities.filter {it.scoreboardTags.contains("arena.game.dungeon_exit") && !it.scoreboardTags.contains("arena.game.dungeon_entrance")}) {
                    if (event.player.world.players.all { entranceEntity.boundingBox.contains(it.location.toVector()) } ) {
                        session.playerChangeRoom(entranceEntity.scoreboardTags.contains("arena.game.dungeon_floor_changer"))
                        break
                    }
                }
            }
        }
    }
}
