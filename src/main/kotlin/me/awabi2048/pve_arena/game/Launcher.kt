package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.game.Launcher.Type.*
import org.bukkit.*
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.generator.ChunkGenerator
import java.io.File
import java.util.*
import javax.annotation.Nonnull

class Launcher(private val type: Type) {
    fun prepareWorld(uuid: String) {

        val sessionWorldCreator = WorldCreator("arena_session.$uuid")
        sessionWorldCreator.environment(World.Environment.NORMAL)
        sessionWorldCreator.type(WorldType.FLAT)

        sessionWorldCreator.generateStructures(false)
        sessionWorldCreator.generator(EmptyChunkGenerator())

        sessionWorldCreator.createWorld()

        // ゲームルール設定
        val sessionWorld = Bukkit.getWorld("arena_session.$uuid")!!

        sessionWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        sessionWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        sessionWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        sessionWorld.setGameRule(GameRule.MOB_GRIEFING, false)
        sessionWorld.setGameRule(GameRule.REDUCED_DEBUG_INFO, true)
        sessionWorld.setGameRule(GameRule.DO_FIRE_TICK, true)

        sessionWorld.setGameRule(GameRule.KEEP_INVENTORY, true)

//        when (stageType) {
//            NORMAL -> TODO()
//            QUICK -> TODO()
//        }
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

    fun prepareStructure(uuid: String) {
        val fileName = when(type) {
            NORMAL -> "normal"
            QUICK -> "normal"
            DUNGEON -> "dungeon_start"
            BOSS_LOBBY -> "boss_lobby"
        }

        val structureManager = Bukkit.getStructureManager()
        structureManager.loadStructure(
            File(
                Main.instance.dataFolder.toString() + File.separator + "structure/$fileName.nbt".replace(
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
    }

    enum class Type {
        NORMAL,
        QUICK,
        DUNGEON,
        BOSS_LOBBY;
    }
}
