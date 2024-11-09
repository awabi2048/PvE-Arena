package me.awabi2048.kota_arena

import me.awabi2048.kota_arena.command.MainCommand
import me.awabi2048.kota_arena.misc.SessionData
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        var prefix = "§7|§cArena§7|"

        var configFile: FileConfiguration? = null
        var playerMenuStatus: MutableMap<Player, String> = mutableMapOf()
        var sessionDataMap: MutableMap<String, SessionData> = mutableMapOf()
        lateinit var instance: JavaPlugin
    }

    override fun onEnable() {
        instance = this

        // Plugin startup logic
        saveDefaultConfig()
        configFile = config

        getCommand("arena")?.setExecutor(MainCommand)

        server.pluginManager.registerEvents(EventListener, instance)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

