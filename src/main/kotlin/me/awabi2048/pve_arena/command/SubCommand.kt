package me.awabi2048.pve_arena.command

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.arenaSessionMap
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.command.SubCommand.Option.*
import me.awabi2048.pve_arena.config.ConfigLoader
import me.awabi2048.pve_arena.game.Generic
import me.awabi2048.pve_arena.misc.sendError
import me.awabi2048.pve_arena.game.Launcher
import me.awabi2048.pve_arena.game.NormalArena
import me.awabi2048.pve_arena.game.QuickArena
import me.awabi2048.pve_arena.misc.Lib
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SubCommand(private val sender: Player, private val args: Array<out String>, private val option: Option) {
    //
    fun execute() {
        fun config() {
            if (args[1] == "reload") {
                try {
                    ConfigLoader.loadAll()
                    sender.sendMessage("$prefix §a全てのコンフィグをリロードしました。")
                } catch (e: Exception) {
                    sender.sendMessage("$prefix §cコンフィグのリロードに失敗しました。")
                }
            }
        }

        fun startSession() {
            try {
                val stageType = Launcher.StageType.valueOf(args[1])

                if (stageType == Launcher.StageType.NORMAL) {
                    val mobType = NormalArena.MobType.valueOf(args[2])
                    val difficulty = NormalArena.MobDifficulty.valueOf(args[3])
                    val players = setOf(sender)
                    val sacrifice = args[4].toInt()
                    val uuid = sender.uniqueId.toString()

//                    val initialStatus = NormalArena.Status(0, players.toMutableSet(), Generic.StatusCode.WAITING_GENERATION)
                    val session =
                        NormalArena(Generic.GenerationData(uuid, players, mobType, difficulty, sacrifice))

                    sender.sendMessage("$prefix §7セッションの開始処理を行っています... (Estimated: 2.0s - 3.0s)")

                    arenaSessionMap[uuid] = session
                    session.generate()
                    Bukkit.getScheduler().runTaskLater(
                        instance,
                        Runnable {
                            session.joinPlayer(sender)
                            session.start()
                        },
                        40L
                    )
                }
            } catch (e: Exception) {
                sender.sendError("無効なセッション情報です: start_session <TYPE> <MobType> <Difficulty> <Sacrifice>")
            }
        }

        fun stopSession() {
            // open
            val menu = Bukkit.createInventory(null, 54, "アリーナセッション管理")
            val black = Lib.getHiddenItem(Material.BLACK_STAINED_GLASS_PANE)
            for (slot in 0..8) {
                menu.setItem(slot, black)
                menu.setItem(slot + 45, black)
            }

            for (index in 0..<arenaSessionMap.size) {
                val uuid = arenaSessionMap.keys.toList()[index]
                val session = arenaSessionMap[uuid]!!
                val type = when(session) {
                    is NormalArena -> "§cNormal"
                    is QuickArena -> "§eQuick"
                    else -> "§cERROR"
                }

                val icon = ItemStack(Material.LIME_CONCRETE)
                val iconMeta = icon.itemMeta
                iconMeta.itemName(Component.text(uuid))
                iconMeta.lore(
                    listOf(
                        Component.text("§7- $type Arena"),
                        Component.text("§f${Bukkit.getWorld("arena_session.$uuid")!!.players}"),
                        Component.text(""),
                        Component.text("§cShift + ホイールクリックでセッションを停止します。"),
                    )
                )

                icon.itemMeta = iconMeta
                menu.setItem(index + 9, icon)
            }
            sender.openInventory(menu)
        }

        when (option) {
            CONFIG -> config()
            START_SESSION -> startSession()
            JOIN_SESSION -> TODO()
            STOP_SESSION -> stopSession()
        }
    }

        enum class Option {
            CONFIG,
            START_SESSION,
            JOIN_SESSION,
            STOP_SESSION,
        }
}
