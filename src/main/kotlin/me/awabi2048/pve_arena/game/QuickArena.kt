package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.arenaStatusMap
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.Main.Companion.quickArenaStatusMap
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective

class QuickArena(private val generationData: GenerationData): Generic(generationData.uuid) {

    override fun generate() {


        val launcher = Launcher(Launcher.StageType.QUICK)
        launcher.prepareWorld(generationData.uuid)
        launcher.prepareStructure(generationData.uuid)

        println("$prefix Started arena session for uuid: ${generationData.uuid}, type: QUICK, STATUS: ${Main.quickArenaStatusMap[generationData.uuid]?.code}")
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
        player.sendMessage("$prefix §7クイックアリーナに入場しました。")

        val displayScoreboard = setupScoreboard(player)
        player.scoreboard = displayScoreboard.scoreboard!!
        Main.displayScoreboardMap[player] = displayScoreboard
    }

    override fun start() {
        getSessionWorld()!!.players.forEach {
            it.sendMessage("$prefix §7Wave 1は§e§n15秒後§7に開始します。")
        }

        Bukkit.getScheduler().runTaskLater(
            Main.instance,
            Runnable {
                countdown(14)
            },
            20L
        )

        Bukkit.getScheduler().runTaskLater(
            Main.instance,
            Runnable {
                waveProcession()
            },
            300L
        )
    }

    override fun waveProcession() {
        arenaStatusMap[uuid]!!.wave += 1
        val wave = quickArenaStatusMap[generationData.uuid]!!.wave

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
                displayScoreboard.scoreboard!!.resetScores("§fMobs §7---")
            } else {
                displayScoreboard.scoreboard!!.resetScores("§fWave §6${wave - 1}§7/10")
                displayScoreboard.scoreboard!!.resetScores("§fMobs §c0§7/10")
            }

            displayScoreboard.getScore("§fWave §6${wave}§7/10").score = 2
            displayScoreboard.getScore("§fMobs §c10§7/10").score = 1
        }

        startSpawnSession()

        if (wave < 10) {
            Bukkit.getScheduler().runTaskLater(
                Main.instance,
                Runnable {
                    waveProcession()
                },
                600L
            )
        }

        if (wave == 1) {
            Bukkit.getScheduler().runTaskLater(
                Main.instance,
                Runnable {
                    timeTracking()
                },
                2L
            )
        }
    }

    override fun setupScoreboard(player: Player): Objective {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard.registerNewObjective("arena_scoreboard_display.${player.uniqueId}", Criteria.DUMMY, "§7« §e§lQuick Arena §7»")
        scoreboard.displaySlot = DisplaySlot.SIDEBAR

        scoreboard.getScore("").score = 5
        scoreboard.getScore("§fTime §700:00.0").score = 4
        scoreboard.getScore("").score = 3
        scoreboard.getScore("§fWave §7---").score = 2
        scoreboard.getScore("§fMobs §7---").score = 1

        return scoreboard
    }

    data class GenerationData(val uuid: String, val players: Set<Player>)
}
