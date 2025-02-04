package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main.Companion.displayScoreboardMap
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.config.DataFile
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import java.io.File
import java.util.*
import kotlin.math.roundToInt

class ArenaDungeon(uuid: String, players: Set<Player>, status: Status, val structureType: StructureType) :
    Generic(uuid, players) {
    private fun roomUpdate(floorChanged: Boolean) {
        val floor = if (status is Status.InGame) {
            (status as Status.InGame).floor
        } else 1
        val room = if (status is Status.InGame) {
            (status as Status.InGame).room
        } else 1

        // 次floorへ進むか判定
        val structureName = if (room >= floor + 2) {
            "stairs_room"
        } else {
            // random choose
            DataFile.dungeonStructure.getStringList(
                "structure_type.${
                    structureType.toString().lowercase()
                }.possible_structure"
            )
                .random()
        }

        // load structure
        val structureManager = Bukkit.getStructureManager()
        structureManager.loadStructure(
            File(
                instance.dataFolder.toString() + "/structure/dungeon/${
                    structureType.toString().lowercase()
                }/$structureName.nbt".replace(
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

        println("${structureType.toString().lowercase()}/$structureName.nbt, room: $room, floor: $floor")

        // status update
        if (status !is Status.InGame) {
            status = Status.InGame(1, 1)
        } else {
            if (floorChanged) {
                (status as Status.InGame).floor += 1
            }

            (status as Status.InGame).room += 1
        }

        // mob
        val summonCount = (8 * ((status as Status.InGame).floor * 0.25 + 1.0)).roundToInt()

        for (spawnLocationEntity in getSessionWorld()!!.entities.filter { it.scoreboardTags.contains("arena.game.dungeon_mob_spawn") }) {
            for (i in 1..summonCount){ mobSpawn(spawnLocationEntity.location) }
        }
    }

    private fun mobSpawn(location: Location) {
        val spawnMobId = DataFile.dungeonStructure.getStringList(
            "structure_type.${
                structureType.toString().lowercase()
            }.possible_mob"
        )
            .random()

        val spawnLocation = Location(location.world, location.x + (-15..15).random() * 0.1, location.y, location.z + (-15..15).random() * 0.1)
        val mob = summonMobFromId(spawnMobId, getSessionWorld()!!, spawnLocation)

        location.world.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, spawnLocation, 10, 0.1, 0.1, 0.1, 0.1)
    }

    fun playerChangeRoom(floorChanged: Boolean) {
        //
        roomUpdate(floorChanged)

        // warp etc
        val entranceLocation =
            getSessionWorld()!!.entities.filter { it.scoreboardTags.contains("arena.game.dungeon_entrance") }
                .random().location

        getSessionWorld()!!.players.forEach {
            it.teleport(entranceLocation)
            it.playSound(it, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 1.8f)

        }
    }

    override fun joinPlayer(player: Player) {
        val initialLocation =
            getSessionWorld()!!.entities.filter { it.scoreboardTags.contains("arena.game.dungeon_entrance") }
                .toList()[0].location.add(0.0, 0.5, 0.0)

        player.teleport(initialLocation)

        // announce
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f)
        player.playSound(player, Sound.ENTITY_VILLAGER_TRADE, 1.0f, 0.75f)
        player.sendMessage("$prefix §3アリーナダンジョン§7に入場しました。")

        // scoreboard
        setupScoreboard(player)
    }

    override fun generate() {
        // status
        this.status = Status.WaitingGeneration

        val launcher = Launcher(Launcher.Type.DUNGEON)

        if (Bukkit.getWorlds().any { it.name == "arena_session.$uuid" }) {
            val sessionWorld = getSessionWorld()!!
            sessionWorld.entities.forEach { it.remove() }
        } else {
            launcher.prepareWorld(uuid)
        }

//        launcher.prepareStructure(uuid)

        println("$prefix Started arena session for uuid: ${uuid}, type: DUNGEON, STATUS: $status")

        // structure load
        roomUpdate(false)

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
        status = Status.InGame(1, 1)
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

    sealed class Status : Generic.Status() {
        data object Standby : Status()
        data object WaitingGeneration : Status()
        data object WaitingStart : Status()
        data class InGame(var floor: Int, var room: Int) : Status()
    }

    enum class StructureType {
        DESERT_TEMPLE,
    }
}
