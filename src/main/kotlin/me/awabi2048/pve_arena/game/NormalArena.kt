package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main.Companion.displayScoreboardMap
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.Main.Companion.spawnSessionKillCount
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.game.WaveProcessingMode.MobDifficulty.*
import me.awabi2048.pve_arena.item.ItemManager
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import kotlin.math.pow
import kotlin.math.roundToInt

class NormalArena(
    uuid: String,
    players: Set<Player>,
    val mobType: WaveProcessingMode.MobType,
    val mobDifficulty: WaveProcessingMode.MobDifficulty,
    val sacrifice: Int = 0,
) : Generic(uuid, players), WaveProcessingMode {

    val lastWave = DataFile.mobDifficulty.getInt("${mobDifficultyToString(mobDifficulty)}.wave")
    override fun generate() {
        status = Status.WaitingGeneration

        val launcher = Launcher(GameType.Normal(mobType, mobDifficulty))

        if (Bukkit.getWorlds().any { it.name == "arena_session.$uuid" }) {
            val sessionWorld = getSessionWorld()!!
            sessionWorld.entities.forEach { it.remove() }
        } else {
            launcher.prepareWorld(uuid)
        }

        launcher.prepareStructure(uuid, DataFile.mobType.getString("${mobTypeToString(mobType)}.structure")!!)

        println("$prefix Started arena session for uuid: ${uuid}, type: NORMAL, STATUS: $status")

        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                status = Status.WaitingStart
            },
            20L
        )

        afkCheck()
    }

    override fun start() {
        getSessionWorld()!!.players.forEach {
            it.sendMessage("$prefix §b最初のウェーブ§7は§e§n20秒後§7に開始します。")
        }
        startCountdown(20, getSessionWorld()!!)

        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                if (getSessionWorld()!!.players.isNotEmpty()) waveProcession()
            },
            400L
        )
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

        // announce
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f)
        player.sendMessage("$prefix §eアリーナ§7に入場しました。")

        // scoreboard
        setupScoreboard(player)

    }

    override fun waveProcession() {
        if (status is Status.WaitingStart) status = Status.InGame(mobType, mobDifficulty, 0, 0)

        (status as Status.InGame).wave += 1
        val wave = (status as Status.InGame).wave

        getSessionWorld()!!.players.forEach {
            // 開始時演出
            it.playSound(it, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f)
            if (wave == lastWave) {
                it.sendTitle("", "§7- §6§lLast Wave §7-", 5, 40, 5)
            } else {
                it.sendTitle("", "§7- §6Wave $wave §7-", 5, 40, 5)
            }

            // scoreboard update
            val displayScoreboard = displayScoreboardMap[it]!!
            if (wave == 1) {
                displayScoreboard.scoreboard!!.resetScores("§fWave §7---")
                displayScoreboard.scoreboard!!.resetScores("§fMobs §7---")
            } else {
                for (count in 0..summonCountCalc(wave - 1)) {
                    displayScoreboard.scoreboard!!.resetScores("§fMobs §c${count}§7/${summonCountCalc(wave - 1)}")
                }
                displayScoreboard.scoreboard!!.resetScores("§fWave §6${wave - 1}§7/$lastWave")
            }

            displayScoreboard.getScore("§fWave §6${wave}§7/$lastWave").score = 2
            displayScoreboard.getScore("§fMobs §c${summonCountCalc(wave)}§7/${summonCountCalc(wave)}").score = 1
        }

        // spawn start
        startSpawnSession()

        if (wave == 1) {
            // タイム計測開始
            timeTracking(3)
            startWatchMobMovement()
        }
    }

    override fun rewardDistribute() {
        // calc reward
        val baseTicket = DataFile.mobType.getInt("${mobTypeToString(mobType)}.reward.ticket")
        val basePoint = DataFile.mobType.getInt("${mobTypeToString(mobType)}.reward.point")
        val baseProfessionExp =
            DataFile.mobType.getInt("${mobTypeToString(mobType)}.reward.level_experience")

        val difficultyMultiplier =
            DataFile.mobDifficulty.getDouble("${mobDifficultyToString(mobDifficulty)}.reward_multiplier")
        val sacrificeMultiplier = (1.0 + sacrifice).pow(0.5)

        val ticketCount = when (mobDifficulty) {
            EXPERT -> (baseTicket * Reward.RewardMultiplier.ticket).roundToInt()
            NIGHTMARE -> (baseTicket * difficultyMultiplier * Reward.RewardMultiplier.ticket / 2).roundToInt()
            else -> (baseTicket * Reward.RewardMultiplier.ticket).roundToInt()
        }

        val point =
            (basePoint * difficultyMultiplier * Reward.RewardMultiplier.point * sacrificeMultiplier).roundToInt()
        val professionExp =
            (baseProfessionExp * difficultyMultiplier * Reward.RewardMultiplier.professionExp * sacrificeMultiplier).roundToInt()

        // distribute
        val ticketItem = when (mobDifficulty) {
            EASY -> ItemManager.ArenaItem.TICKET_EASY
            NORMAL -> ItemManager.ArenaItem.TICKET_NORMAL
            HARD -> ItemManager.ArenaItem.TICKET_HARD
            EXPERT -> ItemManager.ArenaItem.TICKET_EXTREME
            NIGHTMARE -> ItemManager.ArenaItem.TICKET_EXTREME
        }

        Reward.distribute(getSessionWorld()!!.players.toSet(), Pair(ticketItem, ticketCount), point, professionExp)

        // stats
        getSessionWorld()!!.players.forEach { writePlayerData(GameType.Normal(mobType, mobDifficulty), it) }
    }

    fun summonCountCalc(wave: Int): Int {
        val mobData = DataFile.mobType.getConfigurationSection(mobTypeToString(mobType))!!
        val baseValue = mobData.getInt("base_summon_count")

        val difficultyModifier =
            DataFile.mobDifficulty.getDouble("${mobDifficultyToString(mobDifficulty)}.mob_multiplier")
        val waveModifier = 1.0 + wave * DataFile.config.getDouble("mob_stats.per_wave", 0.1)

        val modifiedValue = (baseValue * difficultyModifier * waveModifier).roundToInt()

        return modifiedValue
    }

    override fun startSpawnSession() {
        val wave = (status as Status.InGame).wave
        val summonCount = summonCountCalc(wave)
        spawnSessionKillCount[uuid] = 0

        for (i in 1..summonCount) {
            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    if (getSessionWorld()!!.players.isNotEmpty()) {
                        randomSpawn(getSessionWorld()!!, (status as Status.InGame).wave, mobType, mobDifficulty)
                    }
                },
                (DataFile.config.getInt("misc.game.mob_summon_interval") * i).toLong()
            )
        }
    }

    override fun endProcession(clearTime: Int) {
        val difficultySection = DataFile.mobDifficulty.getConfigurationSection(mobDifficultyToString(mobDifficulty))!!
        val mobTypeSection = DataFile.mobType.getConfigurationSection(mobTypeToString(mobType))!!

        // announce
        val difficultyName = difficultySection.getString("name")!!
        val mobTypeName = mobTypeSection.getString("name")!!
        val arenaName = when (mobDifficulty) {
            EASY -> "§a$difficultyName・${mobTypeName}アリーナ"
            NORMAL -> "§e$difficultyName・${mobTypeName}アリーナ"
            HARD -> "§c§l$difficultyName・${mobTypeName}アリーナ"
            EXPERT -> "§d§l$difficultyName・${mobTypeName}アリーナ"
            NIGHTMARE -> "§b§l$difficultyName・${mobTypeName}アリーナ"
        }

        val players: MutableList<String> = mutableListOf()
        getSessionWorld()!!.players.forEach {
            players += it.displayName
        }

        Bukkit.getServer().onlinePlayers.filter { it.hasPermission("pve_arena.main.receive_announce") }.forEach {
            it.sendMessage(
                "$prefix §e${players.joinToString()} §7さんが$arenaName§7をクリアしました！ §7[§e${
                    Lib.tickToClock(
                        clearTime
                    )
                }§7]"
            )
        }

        // end session
        stop()
    }

    override fun setupScoreboard(player: Player) {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard.registerNewObjective(
            "arena_scoreboard_display.${player.uniqueId}",
            Criteria.DUMMY,
            "§7« §c§lArena §7»"
        )
        scoreboard.displaySlot = DisplaySlot.SIDEBAR
//        scoreboard.numberFormat(NumberFormat.blank())

        val mobTypeName = "§b${mobTypeToString(mobType).capitalize()}"

        val difficultyName = when (mobDifficulty) {
            EASY -> "§a${mobDifficultyToString(mobDifficulty).capitalize()}"
            NORMAL -> "§e${mobDifficultyToString(mobDifficulty).capitalize()}"
            HARD -> "§c${mobDifficultyToString(mobDifficulty).capitalize()}"
            EXPERT -> "§d${mobDifficultyToString(mobDifficulty).capitalize()}"
            NIGHTMARE -> "§4${mobDifficultyToString(mobDifficulty).capitalize()}"
        }

        scoreboard.getScore("").score = 6
        scoreboard.getScore("$mobTypeName §7| $difficultyName").score = 5
        scoreboard.getScore("").score = 4
        scoreboard.getScore("§fTime §700:00.00").score = 3
        scoreboard.getScore("§fWave §7---").score = 2
        scoreboard.getScore("§fMobs §7---").score = 1

        player.scoreboard = scoreboard.scoreboard!!
        displayScoreboardMap[player] = scoreboard
    }
}
