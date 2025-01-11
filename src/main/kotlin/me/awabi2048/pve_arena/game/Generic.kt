package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scoreboard.Objective
import javax.annotation.Nullable

abstract class Generic(val uuid: String, val players: Set<Player>, var status: Status = Status.Standby) {

    val originLocation = Location(Bukkit.getWorld("arena_session.${uuid}"), 0.5, 0.0, 0.5)

    fun getSessionWorld(): World? {
        return Bukkit.getWorld("arena_session.${uuid}")
    }

    fun summonMob(id: String, location: Location): LivingEntity {
        //
        println(id)
        val mobData = DataFile.mobDefinition.getConfigurationSection(id)!!

        // get world
        val world = Bukkit.getWorld("arena_session.${uuid}")!!

        //
        println("ENTITY TYPE: ${mobData.getString("entity_type")}")
        val entityType = EntityType.valueOf(mobData.getString("entity_type") ?: "".uppercase())

        val entity = world.spawnEntity(location, entityType) as LivingEntity
        entity.customName = mobData.getString("display_name") ?: ""
        entity.isCustomNameVisible = true

        // armor
        entity.equipment!!.helmet =
            ItemStack(Material.getMaterial(mobData.getString("equipment.helmet") ?: "AIR") ?: Material.AIR)
        entity.equipment!!.chestplate =
            ItemStack(Material.getMaterial(mobData.getString("equipment.chestplate") ?: "AIR") ?: Material.AIR)
        entity.equipment!!.leggings =
            ItemStack(Material.getMaterial(mobData.getString("equipment.leggings") ?: "AIR") ?: Material.AIR)
        entity.equipment!!.boots =
            ItemStack(Material.getMaterial(mobData.getString("equipment.boots") ?: "AIR") ?: Material.AIR)

        entity.equipment!!.helmetDropChance = 0.0f
        entity.equipment!!.chestplateDropChance = 0.0f
        entity.equipment!!.leggingsDropChance = 0.0f
        entity.equipment!!.bootsDropChance = 0.0f

        // hands
        entity.equipment!!.setItemInMainHand(
            ItemStack(
                Material.getMaterial(
                    mobData.getString("equipment.hand") ?: "AIR"
                ) ?: Material.AIR
            )
        )
        entity.equipment!!.setItemInOffHand(
            ItemStack(
                Material.getMaterial(
                    mobData.getString("equipment.offhand") ?: "AIR"
                ) ?: Material.AIR
            )
        )

        //
        entity.scoreboardTags.add("arena.mob")

        entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE)!!.baseValue = 64.0
        (entity as Monster).target = getSessionWorld()!!.players.random()

        return entity
    }

    fun countdown(timeRemaining: Int) {
        if (timeRemaining == 0) return

        getSessionWorld()!!.players.forEach {
            if (timeRemaining in 1..5) {
                it.playSound(it, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
//                it.sendTitlePart(TitlePart.SUBTITLE, Component.text("§7- §e$timeRemaining §7-")) // NoClassDefFoundException
                it.sendTitle("", "§7- §e$timeRemaining §7-", 5, 20, 5)
            }

            if (timeRemaining in listOf(5, 10, 15)) {
                it.sendMessage("$prefix §7開始まで残り§e§n${timeRemaining}秒§7です。")
                it.sendTitle("", "§7- §e$timeRemaining §7-", 5, 20, 5)
                it.playSound(it, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            }
        }

        Bukkit.getScheduler().runTaskLater(
            Main.instance,
            Runnable {
                countdown(timeRemaining - 1)
            },
            20
        )
    }

    fun timeTracking() {
        (status as Status.InGame).timeElapsed += 1
        val timeElapsed = (status as Status.InGame).timeElapsed

        val timeBefore = Lib.timeToClock(timeElapsed - 1)
        val time = Lib.timeToClock(timeElapsed)

        // scoreboard
        getSessionWorld()!!.players.forEach {
            val displayScoreboard = Main.displayScoreboardMap[it]!!

            displayScoreboard.scoreboard!!.resetScores("§fTime §7$timeBefore")
            if (timeElapsed == 1) displayScoreboard.scoreboard!!.resetScores("§fTime §700:00.0")

            displayScoreboard.getScore("§fTime §7$time").score = 3
        }

        Bukkit.getScheduler().runTaskLater(
            Main.instance,
            Runnable {
                timeTracking()
            },
            2L
        )
    }

    abstract fun joinPlayer(player: Player)
    abstract fun waveProcession()
    abstract fun generate()
    abstract fun start()
    abstract fun setupScoreboard(player: Player): Objective
    abstract fun stop()

    enum class StatusCode {
        WAITING_GENERATION,
        IN_GAME,
        WAITING_FINISH;
    }

    data class GenerationData(
        val uuid: String,
        val players: Set<Player>,
        @Nullable val mobType: NormalArena.MobType,
        @Nullable val difficulty: NormalArena.MobDifficulty,
        @Nullable val sacrifice: Int
    )

//    data class Status(var wave: Int, var code: StatusCode, var timeElapsed: Int)

    sealed class Status {
        data object Standby : Status()
        data object WaitingGeneration : Status()
        data object WaitingStart : Status()
        data class InGame(
            var mobType: NormalArena.MobType,
            var difficulty: NormalArena.MobDifficulty,
            var timeElapsed: Int,
            var wave: Int
        ) : Status()
        data object WaitingFinish : Status()
    }
}
