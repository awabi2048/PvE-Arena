package me.awabi2048.pve_arena

import me.awabi2048.pve_arena.command.MainCommand
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.game.Generic
import me.awabi2048.pve_arena.game.Reward
import me.awabi2048.pve_arena.menu.MenuEventListener
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Objective
import org.codehaus.plexus.util.FileUtils

class Main : JavaPlugin() {
    companion object {
        var prefix = "§7«§cArena§7»"

        val activeSession: MutableSet<Generic> = mutableSetOf()
        val spawnSessionKillCount: MutableMap<String, Int> = mutableMapOf()

        // reward multiplier
        val rewardModifiers: MutableSet<Reward.RewardMultiplier.RewardModifier> = mutableSetOf()

        val displayScoreboardMap: MutableMap<Player, Objective> = mutableMapOf()

        // location
        lateinit var lobbyOriginLocation: Location
//        val lobbyOriginLocation: Location = Location(Bukkit.getWorld("arena"), 0.5, 0.25, 0.5)

        lateinit var instance: JavaPlugin
    }

    override fun onEnable() {
        instance = this

        DataFile.copy()
        DataFile.loadAll()

        lobbyOriginLocation = Location(
            Bukkit.getWorld(DataFile.config.getString("lobby_dimension") ?: "arena"),
            DataFile.config.getInt("lobby_location_x").toDouble() + 0.5,
            DataFile.config.getInt("lobby_location_y").toDouble(),
            DataFile.config.getInt("lobby_location_z").toDouble() + 0.5,
        )

        getCommand("arena")?.setExecutor(MainCommand)

        server.pluginManager.registerEvents(EventListener, instance)
        server.pluginManager.registerEvents(MenuEventListener, instance)


    }

    override fun onDisable() {
        // Plugin shutdown logic
        // remove all session data
        for (sessionWorld in Bukkit.getWorlds().filter { it.name.startsWith("arena_session.") }) {
            sessionWorld.players.forEach {
                it.teleport(lobbyOriginLocation)
                val scoreboard = displayScoreboardMap[it]!!
                scoreboard.unregister()
            }

            Bukkit.unloadWorld(sessionWorld, true)
            FileUtils.deleteDirectory(sessionWorld.name)
        }
    }
}

