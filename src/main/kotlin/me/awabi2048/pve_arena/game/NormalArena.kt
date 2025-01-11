package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.arenaStatusMap
import me.awabi2048.pve_arena.Main.Companion.difficultyConfig
import me.awabi2048.pve_arena.Main.Companion.displayScoreboardMap
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.lobbyOriginLocation
import me.awabi2048.pve_arena.Main.Companion.mainConfig
import me.awabi2048.pve_arena.Main.Companion.mobTypeConfig
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.game.NormalArena.MobDifficulty.*
import me.awabi2048.pve_arena.misc.Lib
import me.awabi2048.pve_arena.misc.PlayerData
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.scoreboard.*
import org.codehaus.plexus.util.FileUtils
import kotlin.math.pow
import kotlin.math.roundToInt

class NormalArena(uuid: String, val mobType: MobType, val difficulty: MobDifficulty, val sacrifice: Int = 0) : Generic(uuid) {

    val lastWave = DataFile.difficulty.getInt("${mobDifficultyToString(difficulty)}.wave")

    private val mobTypeSection = DataFile.mobType.getConfigurationSection(mobTypeToString(mobType))!!
    private val difficultySection = DataFile.difficulty.getConfigurationSection(mobDifficultyToString(difficulty))!!

    override fun generate() {
        status = Status.WaitingGeneration

        val launcher = Launcher(Launcher.StageType.NORMAL)
        launcher.prepareWorld(uuid)
        launcher.prepareStructure(uuid)

        println("$prefix Started arena session for uuid: ${uuid}, type: NORMAL, STATUS: $status")
    }

    override fun start() {
        getSessionWorld()!!.players.forEach {
            it.sendMessage("$prefix §7Wave 1は§e§n20秒後§7に開始します。")
        }
        countdown(20)

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

    fun finishWave() {
        if (status !is Status.InGame) {
            throw IllegalStateException()
        }

        val currentStatus = status as Status.InGame
        getSessionWorld()!!.players.forEach {
            if (currentStatus.wave != lastWave) {
                it.sendMessage("$prefix §6Wave ${currentStatus.wave} §7が終了しました！§e10秒後§7に次のウェーブが開始します。")
                it.playSound(it, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        countdown(9)
                    },
                    20L
                )

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        waveProcession()
                    },
                    200L
                )

            } else {
                it.sendMessage("$prefix §6Last Wave§7 が終了しました！§e10秒後§7にロビーに戻ります。")
                it.playSound(it, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
                it.playSound(it, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.1f)

                // scoreboard
                println("ELAPSED: ${currentStatus.timeElapsed}")
                status = Status.WaitingFinish

                // reward
                rewardDistribute()

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        endProcession()
                    },
                    200L
                )
            }
        }
    }

    override fun waveProcession() {
        if (status !is Status.InGame) throw IllegalStateException()

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

    private fun rewardDistribute() {
        // calc reward
        val baseTicket = DataFile.mobType.getInt("${mobTypeToString(mobType)}.reward.ticket")
        val basePoint = DataFile.mobType.getInt("${mobTypeToString(mobType)}.reward.point")
        val baseProfessionExp =
            DataFile.mobType.getInt("${mobTypeToString(mobType)}.reward.level_experience")

        val difficultyMultiplier =
            DataFile.difficulty.getDouble("${mobDifficultyToString(difficulty)}.reward_multiplier")
        val sacrificeMultiplier = (1.0 + sacrifice).pow(0.5)

        val ticket = when (difficulty) {
            EXPERT -> (baseTicket * Reward.RewardMultiplier.ticket).roundToInt()
            NIGHTMARE -> (baseTicket * difficultyMultiplier * Reward.RewardMultiplier.ticket / 2).roundToInt()
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

    private fun mobStatsCalc(id: String, data: String): Double {

        val mobData = DataFile.mobDefinition.getConfigurationSection(id)!!
        val baseValue = mobData.getDouble("base_stats.$data")

        val difficultyModifier =
            difficultySection.getDouble("mob_multiplier", 1.0)
        val waveModifier =
            1.0 + status.wave * mainConfig!!.getDouble("mob_stats.per_wave", 0.1)
        val playerCountModifier =
            1.0 + (generationData.players.size - 1) * mainConfig!!.getDouble("mob_stats.per_player_count", 0.1)

        val totalModifier = difficultyModifier * waveModifier * playerCountModifier

        val modifiedValue = when (data) {
            "speed" -> baseValue * totalModifier.pow(mainConfig!!.getDouble("mob_stats.speed_modifier_pow", 0.1))
            else -> baseValue * totalModifier
        }

        return modifiedValue
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

    private fun startSpawnSession() {
        if (status !is Status.InGame) throw IllegalStateException()
        val summonCount = summonCountCalc((status as Status.InGame).wave)

        for (i in 1..summonCount) {
            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    randomSpawn()
                },
                (10 * i).toLong()
            )
        }
    }

    private fun mobSelect(): String {
        val availableMobSet = DataFile.mobType
            .getConfigurationSection("${mobTypeToString(mobType)}.mobs")!!.getKeys(false)
        val availableMobSection = DataFile.mobType
            .getConfigurationSection("${generationData.mobType.toString().lowercase()}.mobs")!!

        val status = arenaStatusMap[generationData.uuid]!!
        val wave = status.wave

        val spawnCandidate: MutableList<String> = mutableListOf()
        for (key in availableMobSet) {
            val waveRangeString = availableMobSection.getString("$key.wave")!!
            val waveRangeMin = waveRangeString.substringBefore("..").toInt()
            val waveRangeMax = waveRangeString.substringAfter("..").toInt()
            val waveRange = waveRangeMin..waveRangeMax

            if (wave in waveRange) spawnCandidate += key
        }

        var weightSum = 0

        for (key in spawnCandidate) {
            weightSum += availableMobSection.getInt("$key.weight")
        }

        val seed = (1..weightSum).random()

        var weightSumPreliminary = 0
        var spawnMobId = ""
        for (key in spawnCandidate) {
            if (seed in weightSumPreliminary..<weightSumPreliminary + availableMobSection.getInt("$key.weight")) {
                spawnMobId = key
                break
            }
            weightSumPreliminary += availableMobSection.getInt("$key.weight")
        }
        return spawnMobId
    }

    private fun randomSpawn() {
        val spawnLocation =
            listOf(
                Location(getSessionWorld()!!, 18.5, 0.25, 0.5),
                Location(getSessionWorld()!!, 0.5, 0.25, 18.5),
                Location(getSessionWorld()!!, -18.5, 0.25, 0.5),
                Location(getSessionWorld()!!, 0.5, 0.25, -18.5)
            ).random()

        val id = mobSelect()
        val mob = summonMob(id, spawnLocation)

        // attribute
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = mobStatsCalc(id, "health")
        mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.baseValue = mobStatsCalc(id, "strength")
        mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.baseValue = mobStatsCalc(id, "speed")

        mob.health = mobStatsCalc(id, "health")

        getSessionWorld()!!.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, spawnLocation, 10, 0.1, 0.1, 0.1, 0.1)
    }

    private fun endProcession() {
        val status = arenaStatusMap[generationData.uuid]!!

        // announce
        val difficultyName = difficultyConfig!!.getString("${mobDifficultyToString(difficulty)}.name")!!
        val mobTypeName = mobTypeConfig!!.getString("${mobTypeToString(generationData.mobType)}.name")!!
        val arenaName = when (difficulty) {
            EASY -> "§a$difficultyName・${mobTypeName}アリーナ"
            NORMAL -> "§e$difficultyName・${mobTypeName}アリーナ"
            HARD -> "§c§l$difficultyName・${mobTypeName}アリーナ"
            EXPERT -> "§d$difficultyName・${mobTypeName}アリーナ"
            NIGHTMARE -> "§b$difficultyName・${mobTypeName}アリーナ"
        }

        val players: MutableList<String> = mutableListOf()
        getSessionWorld()!!.players.forEach {
            players += it.displayName
        }

        Bukkit.getServer().onlinePlayers.filter { it.hasPermission("pve_arena.main.receive_announce") }.forEach {
            it.sendMessage(
                "$prefix §e${players.joinToString()}§7さんが$arenaName§7をクリアしました！§f<§e${
                    Lib.timeToClock(
                        status.timeElapsed
                    )
                }§f>"
            )
        }

        // end session
        stopSession()
    }

    fun stopSession() {
        // teleport back
        getSessionWorld()!!.players.forEach {
            it.teleport(lobbyOriginLocation)
            it.playSound(it, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 2.0f)
        }

        // remove session world
        val sessionWorld = getSessionWorld()!!

        Bukkit.unloadWorld(sessionWorld, true)
        FileUtils.deleteDirectory(sessionWorld.name)
    }

    private fun setupScoreboard(player: Player): Objective {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard.registerNewObjective(
            "arena_scoreboard_display.${player.uniqueId}",
            Criteria.DUMMY,
            "§7« §c§lArena §7»"
        )
        scoreboard.displaySlot = DisplaySlot.SIDEBAR
//        scoreboard.numberFormat(NumberFormat.blank())

        val mobTypeName = "§b${mobTypeToString(mobType).lowercase().capitalize()}"

        val difficultyName = when (generationData.difficulty) {
            EASY -> "§a${mobDifficultyToString(difficulty).lowercase().capitalize()}"
            NORMAL -> "§e${mobDifficultyToString(difficulty).lowercase().capitalize()}"
            HARD -> "§c${mobDifficultyToString(difficulty).lowercase().capitalize()}"
            EXPERT -> "§d${mobDifficultyToString(difficulty).lowercase().capitalize()}"
            NIGHTMARE -> "§4${mobDifficultyToString(difficulty).lowercase().capitalize()}"
        }

        scoreboard.getScore("").score = 6
        scoreboard.getScore("$mobTypeName §7| $difficultyName").score = 5
        scoreboard.getScore("").score = 4
        scoreboard.getScore("§fTime §700:00.0").score = 3
        scoreboard.getScore("§fWave §7---").score = 2
        scoreboard.getScore("§fMobs §7---").score = 1

        return scoreboard
    }

    enum class MobType {
        ZOMBIE,
        SKELETON,
        SPIDER,
        BLAZE,
        CREEPER,
        GUARDIAN,
        ENDERMAN;
    }

    private fun mobDifficultyToString(difficulty: MobDifficulty): String {
        return difficulty.toString().substringAfter("MobDifficulty.").lowercase()
    }

    private fun mobTypeToString(mobType: MobType): String {
        return mobType.toString().substringAfter("MobType.").lowercase()
    }

    enum class MobDifficulty {
        EASY,
        NORMAL,
        HARD,
        EXPERT,
        NIGHTMARE;
    }
}
