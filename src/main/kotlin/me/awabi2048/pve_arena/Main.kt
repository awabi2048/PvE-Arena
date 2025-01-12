package me.awabi2048.pve_arena

import me.awabi2048.pve_arena.command.MainCommand
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.game.Generic
import me.awabi2048.pve_arena.game.Reward
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
        val lobbyOriginLocation: Location = Location(Bukkit.getWorld("world"), -110.0, 64.0, -223.0)
//        val lobbyOriginLocation: Location = Location(Bukkit.getWorld("arena"), 0.5, 0.25, 0.5)

        lateinit var instance: JavaPlugin
    }

    override fun onEnable() {
        instance = this

        // Plugin startup logic
        DataFile.copy()
        DataFile.loadAll()

        getCommand("arena")?.setExecutor(MainCommand)

        server.pluginManager.registerEvents(EventListener, instance)


    }

    override fun onDisable() {
        // Plugin shutdown logic
        // remove all session data
        for (sessionWorld in Bukkit.getWorlds().filter{it.name.startsWith("arena_session.")}) {
            sessionWorld.players.forEach {
                it.teleport(lobbyOriginLocation)
            }
            Bukkit.unloadWorld(sessionWorld, false)
            FileUtils.deleteDirectory(sessionWorld.name)
        }
    }
}

