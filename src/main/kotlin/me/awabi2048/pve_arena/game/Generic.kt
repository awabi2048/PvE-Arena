package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.lobbyOriginLocation
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
import org.codehaus.plexus.util.FileUtils
import javax.annotation.Nullable

abstract class Generic(val uuid: String, val players: Set<Player>, var status: Status = Status.Standby) {

    val originLocation = Location(Bukkit.getWorld("arena_session.${uuid}"), 0.5, 0.0, 0.5)

    fun getSessionWorld(): World? {
        return Bukkit.getWorld("arena_session.${uuid}")
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

    fun stop() {
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

    abstract fun joinPlayer(player: Player)
    abstract fun generate()
    abstract fun start()
    abstract fun setupScoreboard(player: Player): Objective
    abstract fun endProcession()

    sealed class Status {
        data object Standby : Status()
        data object WaitingGeneration : Status()
        data object WaitingStart : Status()
        data class InGame(
            var mobType: WaveProcessingMode.MobType,
            var difficulty: WaveProcessingMode.MobDifficulty,
            var timeElapsed: Int,
            var wave: Int
        ) : Status()
        data object WaitingFinish : Status()
    }
}
