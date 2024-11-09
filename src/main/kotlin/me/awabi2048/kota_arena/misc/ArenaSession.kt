package me.awabi2048.kota_arena.misc

import me.awabi2048.kota_arena.Main
import me.awabi2048.kota_arena.config.YamlUtil
import org.bukkit.*
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player
import org.bukkit.generator.ChunkGenerator
import java.io.File
import java.util.*
import javax.annotation.Nonnull

class ArenaSession(private val uuid: String) {
    fun register(players: List<Player>, mobType: Int, difficulty: Int, type: String): Boolean {
        try {
            val sessionData = YamlUtil("session_data.yml").get()

            if (sessionData.contains(uuid)) return false

            val playerUUIDList: MutableList<String> = mutableListOf()
            for (player in players) {
                playerUUIDList + player.uniqueId.toString()
            }

            sessionData.set("$uuid.players", playerUUIDList)
            sessionData.set("$uuid.mob_type", mobType)
            sessionData.set("$uuid.difficulty", difficulty)
            sessionData.set("$uuid.type", type)

            YamlUtil("session_data.yml").save(sessionData)

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun prepareWorld(uuid: String): Boolean {

        // セッションの種類を取得
        val type = YamlUtil("session_data.yml").get().getString("$uuid.type") ?: return false

        // Voidワールドの生成
        try {
            val sessionWorldCreator = WorldCreator("arena_session.$uuid")
            sessionWorldCreator.environment(World.Environment.NORMAL)
            sessionWorldCreator.type(WorldType.FLAT)

            sessionWorldCreator.generateStructures(false)
            sessionWorldCreator.generator(EmptyChunkGenerator())

            sessionWorldCreator.createWorld()

            // ゲームルール設定
            Bukkit.getWorld("arena_session.$uuid")!!.setGameRule(GameRule.DO_MOB_SPAWNING, false)
            Bukkit.getWorld("arena_session.$uuid")!!.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            Bukkit.getWorld("arena_session.$uuid")!!.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            Bukkit.getWorld("arena_session.$uuid")!!.setGameRule(GameRule.MOB_GRIEFING, false)
            Bukkit.getWorld("arena_session.$uuid")!!.setGameRule(GameRule.REDUCED_DEBUG_INFO, true)
            Bukkit.getWorld("arena_session.$uuid")!!.setGameRule(GameRule.DO_FIRE_TICK, true)

            // ストラクチャ読み込み
            val structureManager = Bukkit.getStructureManager()
            structureManager.loadStructure(
                File(
                    Main.instance.dataFolder.toString() + File.separator + "structure/stage/$type.nbt".replace(
                        "/",
                        File.separator
                    )
                )
            ).place(
                Location(Bukkit.getWorld("arena_session.$uuid"), -34.0, -4.0, -34.0),
                true,
                StructureRotation.NONE,
                Mirror.NONE,
                0,
                1.0f,
                Random(0)
            )

            return true

        } catch (e: Exception) {
            return false
        }
    }

    class EmptyChunkGenerator : ChunkGenerator() {
        @Nonnull
        fun chunkDataGeneration(
            @Nonnull world: World?,
            @Nonnull random: Random?,
            x: Int,
            z: Int,
            @Nonnull biome: BiomeGrid?
        ): ChunkData {
            return createChunkData(world!!)
        }
    }

}
