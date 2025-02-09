package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main.Companion.displayScoreboardMap
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import java.io.File
import java.util.*
import kotlin.math.roundToInt

class ArenaDungeon(uuid: String, players: Set<Player>, status: Status, val structureType: StructureType) :
    Generic(uuid, players) {
    val roomCount: Int?
        get() = if (status is Status.InGame) {
            (status as Status.InGame).floor + 2
        } else {
            null
        }

    private fun roomUpdate(floorChanged: Boolean) {
        val floor = if (status is Status.InGame) {
            (status as Status.InGame).floor
        } else 0

        val room = if (status is Status.InGame) {
            (status as Status.InGame).room
        } else 0

        val isFirstEntry = floor == 0 && room == 0

        // ストラクチャ選択
        val structureName = if (roomCount == null) {
            "entrance"
        } else if (room >= roomCount!!) { // ある一定の部屋数を越えたら、次フロアの階段をだすように
            "stairs_room"
        } else { // いずれでもなければ普通に部屋選択
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

        // 初回入場でなければ・・
        if (status is Status.InGame) {

            // モブサモン
            val summonCount = (8 * ((status as Status.InGame).floor * 0.25 + 1.0)).roundToInt()

            for (spawnLocationEntity in getSessionWorld()!!.entities.filter { it.scoreboardTags.contains("arena.game.dungeon_mob_spawn") }) {
                repeat(summonCount) {
                    mobSpawn(spawnLocationEntity.location)
                }
            }

            getSessionWorld()!!.entities.filter { it.scoreboardTags.contains("arena.mob") }.forEach {
                (it as Monster).target = getSessionWorld()!!.players.random()
            }

            //

        }

        // プレイヤーのスコアボード
        getSessionWorld()!!.players.forEach {
            val displayScoreboard = displayScoreboardMap[it]!!
            if (floor == 1 && room == 1) {
                displayScoreboard.scoreboard!!.resetScores("§fFloor §7---")
                displayScoreboard.scoreboard!!.resetScores("§fRoom §7---")
            } else {
                displayScoreboard.scoreboard!!.resetScores("§fRoom §a${room - 1}§7/$roomCount")
            }

            displayScoreboard.getScore("§fRoom §a${room}§7/$roomCount").score = 2
            displayScoreboard.getScore("§fFloor §a${floor}").score = 3

            if (floorChanged) {
                displayScoreboard.scoreboard!!.resetScores("§fFloor §6${floor - 1}§7/$roomCount")
            }
        }

        timeTracking(1)

        // status update
        if (status !is Status.InGame) {
            status = Status.InGame(1, 1, 0)
        } else {
            if (floorChanged) {
                (status as Status.InGame).floor += 1
                (status as Status.InGame).room = 0
            }

            (status as Status.InGame).room += 1
        }
    }

    private fun mobSpawn(location: Location) {
        val spawnCandidate = DataFile.dungeonStructure.getConfigurationSection(
            "structure_type.${
                structureType.toString().lowercase()
            }.possible_mob"
        )!!
        val floor = (status as Status.InGame).floor
        val room = (status as Status.InGame).room

        val spawnCandidateMap = spawnCandidate.getKeys(false) // キー(モブid)をlistでSetで取得
            .filter { floor in Lib.intRangeOf(spawnCandidate.getString("$it.floor")!!)!! } // そのうちウェーブ要件を満たすものをフィルター
            .associateWith { spawnCandidate.getInt("$it.weight") } // 上で選んだキーに重み付けを紐づけ

        val spawnMobId = Lib.simulateWeight(spawnCandidateMap.toMap()) as String

        val spawnLocation = Location(
            location.world,
            location.x + (-15..15).random() * 0.1,
            location.y,
            location.z + (-15..15).random() * 0.1
        )
        val mob = summonMobFromId(spawnMobId, getSessionWorld()!!, spawnLocation) as LivingEntity

        // status
        val health = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        val strength = mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value
        val speed = mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.value

        val progressModifier = (floor + room / roomCount!!) * 0.1

        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = health * (1 + progressModifier)
        mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.baseValue = strength * (1 + progressModifier)
        mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.baseValue = speed * (1 + progressModifier * 0.1)
    }

    fun playerChangeRoom(floorChanged: Boolean) {
        //
        roomUpdate(floorChanged)

        // warp etc
        val entranceEntity =
            getSessionWorld()!!.entities.filter { it.scoreboardTags.contains("arena.game.dungeon_entrance") && !it.scoreboardTags.contains("arena.temp.dungeon_used_entrance")}
                .random()

        val entranceLocation = entranceEntity.location
        getSessionWorld()!!.entities.filter{it.scoreboardTags.contains("arena.temp.dungeon_used_entrance")}.forEach{it.removeScoreboardTag("arena.temp.dungeon_used_entrance")}
        entranceEntity.scoreboardTags.add("arena.temp.dungeon_used_entrance")

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

        val launcher = Launcher(GameType.Dungeon)

        if (Bukkit.getWorlds().any { it.name == "arena_session.$uuid" }) {
            val sessionWorld = getSessionWorld()!!
            sessionWorld.entities.forEach { it.remove() }
        } else {
            launcher.prepareWorld(uuid)
        }

//        launcher.prepareStructure(uuid)

        println("$prefix Started arena session for uuid: ${uuid}, type: DUNGEON, STATUS: $status")

        // structure load
        roomUpdate(true)

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
        status = Status.InGame(1, 1, 0)
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
        data class InGame(var floor: Int, var room: Int, var timeElapsed: Int) : Status()
    }

    enum class StructureType {
        DESERT_TEMPLE,
    }
}
