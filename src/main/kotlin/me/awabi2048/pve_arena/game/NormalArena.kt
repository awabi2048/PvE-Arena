package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main.Companion.displayScoreboardMap
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.Main.Companion.spawnSessionKillCount
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.misc.Lib
import me.awabi2048.pve_arena.misc.PlayerData
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import kotlin.math.pow
import kotlin.math.roundToInt

class NormalArena(
    uuid: String,
    players: Set<Player>,
    val mobType: WaveProcessingMode.MobType,
    val difficulty: WaveProcessingMode.MobDifficulty,
    val sacrifice: Int = 0,
) : Generic(uuid, players), WaveProcessingMode {

    val lastWave = DataFile.difficulty.getInt("${mobDifficultyToString(difficulty)}.wave")

    override fun generate() {
        status = Status.WaitingGeneration

        val launcher = Launcher(Launcher.StageType.NORMAL)
        launcher.prepareWorld(uuid)
        launcher.prepareStructure(uuid)

        println("$prefix Started arena session for uuid: ${uuid}, type: NORMAL, STATUS: $status")

        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                status = Status.WaitingStart
            },
            20L
        )
    }

    override fun start() {
        getSessionWorld()!!.players.forEach {
            it.sendMessage("$prefix §b最初のウェーブ§7は§e§n20秒後§7に開始します。")
        }
        startCountdown(20, getSessionWorld()!!)

        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                waveProcession()
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
        player.sendMessage("$prefix §7アリーナに入場しました。")

        // scoreboard
        val displayScoreboard = setupScoreboard(player)
        player.scoreboard = displayScoreboard.scoreboard!!
        displayScoreboardMap[player] = displayScoreboard
    }

    override fun waveProcession() {
        if (status is Status.WaitingStart) status = Status.InGame(mobType, difficulty, 0, 0)

        (status as Status.InGame).wave += 1
        val wave = (status as Status.InGame).wave

        getSessionWorld()!!.players.forEach {
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
                displayScoreboard.scoreboard!!.resetScores("§fWave §6${wave - 1}§7/$lastWave")
                displayScoreboard.scoreboard!!.resetScores("§fMobs §c0§7/${summonCountCalc(wave - 1)}")
            }

            displayScoreboard.getScore("§fWave §6${wave}§7/$lastWave").score = 2
            displayScoreboard.getScore("§fMobs §c${summonCountCalc(wave)}§7/${summonCountCalc(wave)}").score = 1
        }

        startSpawnSession()

        if (wave == 1) {
            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    timeTracking()
                },
                5L
            )
        }
    }

    override fun rewardDistribute() {
        // calc reward
        val baseTicket = DataFile.mobType.getInt("${mobTypeToString(mobType)}.reward.ticket")
        val basePoint = DataFile.mobType.getInt("${mobTypeToString(mobType)}.reward.point")
        val baseProfessionExp =
            DataFile.mobType.getInt("${mobTypeToString(mobType)}.reward.level_experience")

        val difficultyMultiplier =
            DataFile.difficulty.getDouble("${mobDifficultyToString(difficulty)}.reward_multiplier")
        val sacrificeMultiplier = (1.0 + sacrifice).pow(0.5)

        val ticket = when (difficulty) {
            WaveProcessingMode.MobDifficulty.EXPERT -> (baseTicket * Reward.RewardMultiplier.ticket).roundToInt()
            WaveProcessingMode.MobDifficulty.NIGHTMARE -> (baseTicket * difficultyMultiplier * Reward.RewardMultiplier.ticket / 2).roundToInt()
            else -> (baseTicket * Reward.RewardMultiplier.ticket).roundToInt()
        }

        val point =
            (basePoint * difficultyMultiplier * Reward.RewardMultiplier.point * sacrificeMultiplier).roundToInt()
        val professionExp =
            (baseProfessionExp * difficultyMultiplier * Reward.RewardMultiplier.professionExp * sacrificeMultiplier).roundToInt()

        // distribute
        val ticketItem = Reward.getTicketItem(
            DataFile.difficulty.getString("${mobDifficultyToString(difficulty)}.reward_ticket") ?: ""
        )
        ticketItem.amount = ticket

        // announce
        getSessionWorld()!!.players.forEach {
            it.inventory.addItem(ticketItem)

            val playerStats = PlayerData(it)
            playerStats.addExp(professionExp)
            playerStats.addArenaPoint(point)

            it.sendMessage("§8-----------------------------------------")
            it.sendMessage("$prefix §e報酬を受け取りました！")
            it.sendMessage("§7➠ ${ticketItem.itemMeta.itemName} §fx${ticket}")
            it.sendMessage("§7➠ §bアリーナポイント $point Point")
            it.sendMessage("§7➠ §aアリーナ経験値 $professionExp")
            it.sendMessage("§8-----------------------------------------")
        }
    }

    fun summonCountCalc(wave: Int): Int {
        val mobData = DataFile.mobType.getConfigurationSection(mobTypeToString(mobType))!!
        val baseValue = mobData.getInt("base_summon_count")

        val difficultyModifier =
            DataFile.difficulty.getDouble("${mobDifficultyToString(difficulty)}.mob_multiplier")
        val waveModifier = 1.0 + wave * 0.1

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
                    randomSpawn(getSessionWorld()!!, (status as Status.InGame).wave, mobType, difficulty)
                },
                (10 * i).toLong()
            )
        }
    }

    override fun endProcession() {
        val difficultySection = DataFile.difficulty.getConfigurationSection(mobDifficultyToString(difficulty))!!
        val mobTypeSection = DataFile.mobType.getConfigurationSection(mobTypeToString(mobType))!!

        // announce
        val difficultyName = difficultySection.getString("name")!!
        val mobTypeName = mobTypeSection.getString("name")!!
        val arenaName = when (difficulty) {
            WaveProcessingMode.MobDifficulty.EASY -> "§a$difficultyName・${mobTypeName}アリーナ"
            WaveProcessingMode.MobDifficulty.NORMAL -> "§e$difficultyName・${mobTypeName}アリーナ"
            WaveProcessingMode.MobDifficulty.HARD -> "§c§l$difficultyName・${mobTypeName}アリーナ"
            WaveProcessingMode.MobDifficulty.EXPERT -> "§d§l$difficultyName・${mobTypeName}アリーナ"
            WaveProcessingMode.MobDifficulty.NIGHTMARE -> "§b§l$difficultyName・${mobTypeName}アリーナ"
        }

        val players: MutableList<String> = mutableListOf()
        getSessionWorld()!!.players.forEach {
            players += it.displayName
        }

        Bukkit.getServer().onlinePlayers.filter { it.hasPermission("pve_arena.main.receive_announce") }.forEach {
            it.sendMessage(
                "$prefix §e${players.joinToString()}§7さんが$arenaName§7をクリアしました！§f<§e${
                    Lib.timeToClock(
                        (status as Status.InGame).timeElapsed
                    )
                }§f>"
            )
        }

        // end session
        stop()
    }

    override fun setupScoreboard(player: Player): Objective {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard.registerNewObjective(
            "arena_scoreboard_display.${player.uniqueId}",
            Criteria.DUMMY,
            "§7« §c§lArena §7»"
        )
        scoreboard.displaySlot = DisplaySlot.SIDEBAR
//        scoreboard.numberFormat(NumberFormat.blank())

        val mobTypeName = "§b${mobTypeToString(mobType).capitalize()}"

        val difficultyName = when (difficulty) {
            WaveProcessingMode.MobDifficulty.EASY -> "§a${mobDifficultyToString(difficulty).capitalize()}"
            WaveProcessingMode.MobDifficulty.NORMAL -> "§e${mobDifficultyToString(difficulty).capitalize()}"
            WaveProcessingMode.MobDifficulty.HARD -> "§c${mobDifficultyToString(difficulty).capitalize()}"
            WaveProcessingMode.MobDifficulty.EXPERT -> "§d${mobDifficultyToString(difficulty).capitalize()}"
            WaveProcessingMode.MobDifficulty.NIGHTMARE -> "§4${mobDifficultyToString(difficulty).capitalize()}"
        }

        scoreboard.getScore("").score = 6
        scoreboard.getScore("$mobTypeName §7| $difficultyName").score = 5
        scoreboard.getScore("").score = 4
        scoreboard.getScore("§fTime §700:00.0").score = 3
        scoreboard.getScore("§fWave §7---").score = 2
        scoreboard.getScore("§fMobs §7---").score = 1

        return scoreboard
    }
}
