package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.Main.Companion.spawnSessionKillCount
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.item.ItemManager
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective

class QuickArena(uuid: String, players: Set<Player>): Generic(uuid, players), WaveProcessingMode {

    val lastWave = 10

    override fun generate() {
        status = Status.WaitingGeneration

        val launcher = Launcher(Launcher.Type.QUICK)
        launcher.prepareWorld(uuid)
        launcher.prepareStructure(uuid)

        println("$prefix Started arena session for uuid: ${uuid}, type: QUICK, STATUS: $status")

        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                status = Status.WaitingStart
            },
            20L
        )

        afkCheck()
    }

    override fun joinPlayer(player: Player) {
        player.teleport(
            Location(
                getSessionWorld()!!,
                (-15..15).random().toDouble() / 10,
                0.0,
                (-15..15).random().toDouble() / 10
            )
        )

        player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 2.0f)
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f)
        player.sendMessage("$prefix §eクイックアリーナ§7に入場しました。")

        val displayScoreboard = setupScoreboard(player)
        player.scoreboard = displayScoreboard.scoreboard!!
        Main.displayScoreboardMap[player] = displayScoreboard
    }

    override fun start() {
        getSessionWorld()!!.players.forEach {
            it.sendMessage("$prefix §b最初のウェーブ§7は§e§n15秒後§7に開始します。")
        }

        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                startCountdown(14, getSessionWorld()!!)
            },
            20L
        )

        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                waveProcession()
            },
            300L
        )
    }

    override fun waveProcession() {
        if (status is Status.WaitingStart) status = Status.InGame(WaveProcessingMode.MobType.entries.random(), WaveProcessingMode.MobDifficulty.NORMAL, 0, 0)

        (status as Status.InGame).wave += 1
        val wave = (status as Status.InGame).wave

        getSessionWorld()!!.players.forEach {
            it.playSound(it, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f)
            if (wave == 10) {
                it.sendTitle("", "§7- §6§lLast Wave §7-", 5, 40, 5)
            } else {
                it.sendTitle("", "§7- §6Wave $wave §7-", 5, 40, 5)
            }

            // scoreboard update
            val displayScoreboard = Main.displayScoreboardMap[it]!!
            if (wave == 1) {
                displayScoreboard.scoreboard!!.resetScores("§fWave §7---")
            } else {
                displayScoreboard.scoreboard!!.resetScores("§fWave §6${wave - 1}§7/10")
            }

            displayScoreboard.getScore("§fWave §6${wave}§7/10").score = 2
        }

        startSpawnSession()

        if (wave < 10) {
            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    waveProcession()
                },
                (DataFile.config.getInt("misc.game.quick_arena_time") * 20).toLong()
            )
        }

        if (wave == 1) {
            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    timeTracking()
                },
                1L
            )
        }
    }

    override fun setupScoreboard(player: Player): Objective {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard.registerNewObjective("arena_scoreboard_display.${player.uniqueId}", Criteria.DUMMY, "§7« §e§lQuick Arena §7»")
        scoreboard.displaySlot = DisplaySlot.SIDEBAR
//        scoreboard.numberFormat(NumberFormat.blank()) → 1.21.4までおあずけ

        scoreboard.getScore("").score = 5
        scoreboard.getScore("§fTime §700:00.00").score = 4
        scoreboard.getScore("").score = 3
        scoreboard.getScore("§fWave §7---").score = 2

        return scoreboard
    }

    override fun rewardDistribute() {
        // TODO: 1日1回のスロット的なのを足す

        val ticketCount = 5
        val ticketType = ItemManager.ArenaItem.TICKET_EXTREME
        val point = 1
        val exp = 20

        Reward.distribute(getSessionWorld()!!.players.toSet(), Pair(ticketType, ticketCount), point, exp)
    }

    override fun startSpawnSession() {
        // roll mobs
        val mobType = WaveProcessingMode.MobType.entries.random()
        spawnSessionKillCount[uuid] = 0

        for (i in 1..12) {
            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    if (getSessionWorld()!!.players.isNotEmpty()) {
                        randomSpawn(
                            world = getSessionWorld()!!,
                            wave = (status as Status.InGame).wave,
                            mobType = mobType,
                            difficulty = WaveProcessingMode.MobDifficulty.NORMAL
                        )
                    }
                },
                (10 * i).toLong()
            )
        }
    }

    override fun endProcession(clearTime: Int) {
        // announce
        val players: MutableList<String> = mutableListOf()
        getSessionWorld()!!.players.forEach {
            players += it.displayName
        }

        Bukkit.getServer().onlinePlayers.filter { it.hasPermission("pve_arena.main.receive_announce") }.forEach {
            it.sendMessage(
                "$prefix §e${players.joinToString()} §7さんが§eクイックアリーナ§7をクリアしました！ §7[§e${
                    Lib.tickToClock(
                        (status as Status.InGame).timeElapsed
                    )
                }§7]"
            )
        }

        // end session
        stop()
    }
}
