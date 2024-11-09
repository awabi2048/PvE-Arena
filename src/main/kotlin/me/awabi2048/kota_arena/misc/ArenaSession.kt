package me.awabi2048.kota_arena.misc

import me.awabi2048.kota_arena.Main
import me.awabi2048.kota_arena.Main.Companion.sessionDataMap
import me.awabi2048.kota_arena.config.YamlUtil
import org.bukkit.*
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.generator.ChunkGenerator
import java.io.File
import java.util.*
import javax.annotation.Nonnull

object ArenaSession {
    fun register(uuid: String, mobType: MobType, difficulty: StageDifficulty, type: StageType): Boolean {
        try {
            if (sessionDataMap.contains(uuid)) return false
            sessionDataMap[uuid] = SessionData(mobType, difficulty, type)

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun prepareWorld(uuid: String): Boolean {

        // セッションの種類を取得
        val type = sessionDataMap[uuid]?.type ?: return false

        // Voidワールドの生成
        try {
            val sessionWorldCreator = WorldCreator("arena_session.$uuid")
            sessionWorldCreator.environment(World.Environment.NORMAL)
            sessionWorldCreator.type(WorldType.FLAT)

            sessionWorldCreator.generateStructures(false)
            sessionWorldCreator.generator(EmptyChunkGenerator())

            sessionWorldCreator.createWorld()

            // ゲームルール設定
            val sessionWorld = Bukkit.getWorld("arena_session.$uuid")?: return false

            sessionWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false)
            sessionWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            sessionWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            sessionWorld.setGameRule(GameRule.MOB_GRIEFING, false)
            sessionWorld.setGameRule(GameRule.REDUCED_DEBUG_INFO, true)
            sessionWorld.setGameRule(GameRule.DO_FIRE_TICK, true)

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

    private class EmptyChunkGenerator : ChunkGenerator() {
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

data class SessionData(val mobType: MobType, val difficulty: StageDifficulty, val type: StageType)
