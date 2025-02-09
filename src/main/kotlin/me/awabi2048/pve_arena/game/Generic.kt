package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.activeSession
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.lobbyOriginLocation
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.config.YamlUtil
import me.awabi2048.pve_arena.misc.Lib
import me.awabi2048.pve_arena.quest.GenericQuest
import me.awabi2048.pve_arena.quest.GenericQuest.Criteria.*
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

abstract class Generic(val uuid: String, val players: Set<Player>, var status: Status = Status.Standby) {

    val originLocation = Location(Bukkit.getWorld("arena_session.${uuid}"), 0.5, 0.0, 0.5)

    fun summonMobFromId(id: String, world: World, location: Location): Entity {
        val mobData = DataFile.mobDefinition.getConfigurationSection(id)!!
        val entityType = EntityType.valueOf(mobData.getString("entity_type") ?: throw IllegalStateException("Invalid entity type for $id specified. @stage_data/mob_definition.yml"))

        val mob = world.spawnEntity(location, entityType) as LivingEntity
        mob.customName = mobData.getString("display_name")
        mob.isCustomNameVisible = true

        // armor
        mob.equipment!!.helmet =
            ItemStack(Material.getMaterial(mobData.getString("equipment.helmet") ?: "AIR") ?: Material.AIR)
        mob.equipment!!.chestplate =
            ItemStack(Material.getMaterial(mobData.getString("equipment.chestplate") ?: "AIR") ?: Material.AIR)
        mob.equipment!!.leggings =
            ItemStack(Material.getMaterial(mobData.getString("equipment.leggings") ?: "AIR") ?: Material.AIR)
        mob.equipment!!.boots =
            ItemStack(Material.getMaterial(mobData.getString("equipment.boots") ?: "AIR") ?: Material.AIR)

        mob.equipment!!.helmetDropChance = -1.0f
        mob.equipment!!.chestplateDropChance = -1.0f
        mob.equipment!!.leggingsDropChance = -1.0f
        mob.equipment!!.bootsDropChance = -1.0f

        // hands
        mob.equipment!!.setItemInMainHand(
            ItemStack(
                Material.getMaterial(
                    mobData.getString("equipment.hand") ?: "AIR"
                ) ?: Material.AIR
            )
        )
        mob.equipment!!.setItemInOffHand(
            ItemStack(
                Material.getMaterial(
                    mobData.getString("equipment.offhand") ?: "AIR"
                ) ?: Material.AIR
            )
        )

        mob.equipment!!.itemInMainHandDropChance = -1.0f
        mob.equipment!!.itemInOffHandDropChance = -1.0f

        // attribute
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = mobData.getDouble("base_stats.health")
        mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.baseValue = mobData.getDouble("base_stats.strength")
        mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.baseValue = mobData.getDouble("base_stats.speed")

        val scaleModifier = (90..110).random().toDouble() / 100
        mob.getAttribute(Attribute.GENERIC_SCALE)!!.baseValue = mobData.getDouble("scale") * scaleModifier

        mob.health = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue

        // 識別タグ
        mob.scoreboardTags.add("arena.mob")

        if (mob is Zombie) mob.isBaby = false
        mob.maximumNoDamageTicks = 2

        mob.getAttribute(Attribute.GENERIC_FOLLOW_RANGE)!!.baseValue = 64.0

        return mob
    }

    fun getSessionWorld(): World? {
        return Bukkit.getWorld("arena_session.${uuid}")
    }

    fun timeTracking(order: Int) {
        object : BukkitRunnable() {
            override fun run() {
                try { // タイム加算
                    (status as Status.InGame).timeElapsed += 1
                    val timeElapsed = (status as Status.InGame).timeElapsed

                    val timeBefore = Lib.tickToClock(timeElapsed - 1)
                    val time = Lib.tickToClock(timeElapsed)

                    // スコアボード表示再設定
                    getSessionWorld()!!.players.forEach {
                        val displayScoreboard = Main.displayScoreboardMap[it]!!

                        displayScoreboard.scoreboard!!.resetScores("§fTime §7$timeBefore")
                        if (timeElapsed == 1) displayScoreboard.scoreboard!!.resetScores("§fTime §700:00.00")

                        displayScoreboard.getScore("§fTime §7$time").score = order
                    }
                } catch (e: Exception) {
                    cancel()
                    return
                }

                if (getSessionWorld()!!.players.isEmpty()) cancel()
            }
        }.runTaskTimer(instance, 0, 1)
    }

    fun stop() {
        // teleport back
        getSessionWorld()!!.players.forEach {
            it.teleport(lobbyOriginLocation)
            it.playSound(it, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 2.0f)
        }

//        val worldFile = getSessionWorld()!!.worldFolder
//        Bukkit.unloadWorld(getSessionWorld()!!, false) // ← セーブすると大遅延が発生
        getSessionWorld()!!.entities.forEach {it.remove()}
        activeSession.remove(this)
    }

    fun afkCheck() {
        val secondToTimeout = DataFile.config.getInt("misc.game.second_until_timeout").toLong()

        object : BukkitRunnable() {
            override fun run() {
                if (players.filter { it.noActionTicks >= secondToTimeout }.size == players.size) {
                    players.forEach {
                        it.sendMessage("$prefix §cセッションがタイムアウトしました。")
                        it.playSound(it, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f)
                    }
                    stop()
                    cancel()
                } else if (Lib.lookForSession(uuid) == null) {
                    cancel()
                }
            }
        }.runTaskTimer(instance, 0, secondToTimeout)
    }

    fun startWatchMobMovement() {
        object: BukkitRunnable() {
            override fun run() {
                val sessionWorld = getSessionWorld()!!
                sessionWorld.entities.filterIsInstance<Blaze>().forEach {
                    if (it.velocity.y > 0.0) it.velocity.y = 0.0
                }

                if (status !is Status.InGame) cancel()
            }
        }.runTaskTimer(instance, 0, 1)
    }

    fun writePlayerData(gameType: GameType, player: Player) {
        // コレクション加算
            val path = when(gameType) {
                is GameType.Normal -> "${player.uniqueId}.record.clear_count_collection.${gameType.mobType!!.name.lowercase()}"
                is GameType.Quick -> "${player.uniqueId}.record.clear_count_quick."
                else -> null
            }!!

        val collectionCount = when(gameType) {
            is GameType.Normal -> DataFile.mobDifficulty.getInt("${gameType.mobDifficulty!!.name.lowercase()}.collection_weight")
            is GameType.Quick -> 1
            else -> 0
        }

        DataFile.playerData.set(path, DataFile.stats.getInt(path) + collectionCount)
        DataFile.playerData.save("player_data/main.yml")

        // クエスト処理
        // daily
        for (dailyQuest in DataFile.ongoingQuestData.getConfigurationSection("daily")!!.getKeys(false) - "date") {
            println("checking: $dailyQuest")

            val dataSection = DataFile.ongoingQuestData.getConfigurationSection("daily.$dailyQuest")!!
            val criteria = GenericQuest.Criteria.valueOf(dataSection.getString("criteria")!!.substringBefore("."))

            // 各種値を取得
            val goalValue = dataSection.getInt("value")
            val currentValue = DataFile.playerQuestData.getInt("${player.uniqueId}.daily_$dailyQuest.current")
            val hasCompleted = DataFile.playerQuestData.getBoolean("${player.uniqueId}.daily_$dailyQuest.has_completed")
            println("criteria = $criteria goal value = $goalValue, current value = $currentValue, has completed = $hasCompleted")

            if (hasCompleted) continue

            // このクエスト項目がチェックを必要とするか
            val requireChecking = when(criteria) {
                GAIN_EXP -> true
                CLEAR_TIME_QUICK -> gameType is GameType.Quick
                CLEAR_COUNT_ALL -> true
                CLEAR_COUNT_QUICK -> gameType is GameType.Quick
                CLEAR_COUNT_BOSS -> gameType is GameType.Boss
                CLEAR_COUNT_MOB_TYPE -> gameType is GameType.Normal
                CLEAR_COUNT_MOB_DIFFICULTY -> gameType is GameType.Normal
                SLAY_MOB -> false
            }
            println("require checking = $requireChecking")
            if (!requireChecking) continue

            // GAIN_EXPのみ特殊（加算される値が1でない）なので、先に処理
            if (criteria == GenericQuest.Criteria.GAIN_EXP) {
                continue
            }

            val countedAsQuestCompletion = when(criteria) {
                CLEAR_COUNT_MOB_TYPE -> {
                    val objectiveMobType = dataSection.getString("criteria")!!.substringAfter(".")
                    println(objectiveMobType)
                    (gameType as GameType.Normal).mobType == WaveProcessingMode.MobType.valueOf(objectiveMobType)
                }
                CLEAR_COUNT_MOB_DIFFICULTY -> {
                    val objectiveMobDifficulty = dataSection.getString("criteria")!!.substringAfter(".")
                    (gameType as GameType.Normal).mobDifficulty!!.ordinal >= WaveProcessingMode.MobDifficulty.valueOf(objectiveMobDifficulty).ordinal
                }
                in listOf(CLEAR_COUNT_BOSS, CLEAR_COUNT_QUICK, CLEAR_COUNT_ALL) -> true // 合っていれば何でも良い族
                CLEAR_TIME_QUICK -> {
                    val objectiveTime = dataSection.getString("criteria")!!.substringAfter(".").toInt()
                    val clearTime = (status as Status.InGame).timeElapsed
                    clearTime <= objectiveTime
                }
                else -> false
            }
            println("counted as quest completion = $countedAsQuestCompletion")

            if (countedAsQuestCompletion) {
                DataFile.playerQuestData.set("${player.uniqueId}.daily.$dailyQuest.current", currentValue + 1)
                YamlUtil.save("player_data/quest.yml", DataFile.playerQuestData)
            }
        }
    }

    abstract fun joinPlayer(player: Player)
    abstract fun generate()
    abstract fun start()
    abstract fun setupScoreboard(player: Player)
    abstract fun endProcession(clearTime: Int)

    sealed class Status {
        data object Standby : Status()
        data object WaitingGeneration : Status()
        data object WaitingStart : Status()
        data class InGame(
            var mobType: WaveProcessingMode.MobType,
            var difficulty: WaveProcessingMode.MobDifficulty,
            var timeElapsed: Int,
            var wave: Int,
        ) : Status()

        data object WaitingFinish : Status()
    }
}
