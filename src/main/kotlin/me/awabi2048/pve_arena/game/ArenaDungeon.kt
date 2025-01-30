package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.displayScoreboardMap
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.config.DataFile
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import java.io.File
import java.util.*

class ArenaDungeon(uuid: String, players: Set<Player>, status: Status, val structureType: StructureType): Generic(uuid, players) {
    fun changeRoom() {
        // choose
        val structure = DataFile.dungeonStructure.getStringList("structure_type.${structureType.toString().lowercase()}").random()

        // load structure
        val structureManager = Bukkit.getStructureManager()
        structureManager.loadStructure(
            File(
                instance.dataFolder.toString() + "/structure/dungeon/$structure.nbt".replace(
                    "/",
                    File.separator
                )
            )
        ).place(
            Location(Bukkit.getWorld("arena_session.$uuid"), 0.0, 0.0, 0.0),
            true,
            StructureRotation.NONE,
            Mirror.NONE,
            0,
            1.0f,
            Random(0)
        )

        // warp


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
        player.playSound(player, Sound.ENTITY_VILLAGER_TRADE, 1.0f, 0.75f)
        player.sendMessage("$prefix §3アリーナダンジョン§7に入場しました。")

        // scoreboard
        setupScoreboard(player)
    }

    override fun generate() {
        // status
        status = Status.WaitingGeneration

        val launcher = Launcher(Launcher.Type.DUNGEON)

        if (Bukkit.getWorlds().any { it.name == "arena_session.$uuid" }) {
            val sessionWorld = getSessionWorld()!!
            sessionWorld.entities.forEach { it.remove() }
        } else {
            launcher.prepareWorld(uuid)
        }

        launcher.prepareStructure(uuid)

        println("$prefix Started arena session for uuid: ${uuid}, type: DUNGEON, STATUS: $status")

        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                status = Generic.Status.WaitingStart
            },
            20L
        )

        // start afk check
        afkCheck()
    }

    override fun start() {
        // startup logic

    }

    override fun setupScoreboard(player: Player) {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard.registerNewObjective(
            "arena_scoreboard_display.${player.uniqueId}",
            Criteria.DUMMY,
            "§7« §3§lArena Dungeon §7»"
        )

        scoreboard.displaySlot = DisplaySlot.SIDEBAR

        scoreboard.getScore("").score = 5
        scoreboard.getScore("§fFloor §7---").score = 4
        scoreboard.getScore("§fRoom §7---").score = 3
        scoreboard.getScore("").score = 2
        scoreboard.getScore("§fTime §700:00.00").score = 1

        player.scoreboard = scoreboard.scoreboard!!
        displayScoreboardMap[player] = scoreboard
    }

    override fun endProcession(clearTime: Int) {
        TODO("Not yet implemented")
    }

    sealed class Status: Generic.Status() {
        data object Standby: Status()
        data object WaitingGeneration: Status()
        data object WaitingStart: Status()
        data class InGame(val floor: Int, ): Status()
    }

    enum class StructureType {
        DESERT_TEMPLE,
    }
}
