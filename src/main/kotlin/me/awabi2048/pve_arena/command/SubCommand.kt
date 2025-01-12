package me.awabi2048.pve_arena.command

import me.awabi2048.pve_arena.Main.Companion.activeSession
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.command.SubCommand.Option.*
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.game.Launcher
import me.awabi2048.pve_arena.game.NormalArena
import me.awabi2048.pve_arena.game.QuickArena
import me.awabi2048.pve_arena.game.WaveProcessingMode
import me.awabi2048.pve_arena.misc.Lib
import me.awabi2048.pve_arena.misc.sendError
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
                    DataFile.loadAll()
                    sender.sendMessage("$prefix §a全てのコンフィグをリロードしました。")
                } catch (e: Exception) {
                    sender.sendMessage("$prefix §cコンフィグのリロードに失敗しました。")
                }
            }
        }

        fun startSession() {
            try {
                val stageType = Launcher.StageType.valueOf(args[1])

                val uuid = sender.uniqueId.toString()
                val players = setOf(sender)

                if (stageType == Launcher.StageType.NORMAL) {
                    val mobType = WaveProcessingMode.MobType.valueOf(args[2])
                    val difficulty = WaveProcessingMode.MobDifficulty.valueOf(args[3])
                    val sacrifice = args[4].toInt()

//                    val initialStatus = NormalArena.Status(0, players.toMutableSet(), Generic.StatusCode.WAITING_GENERATION)
                    val session = NormalArena(uuid, players, mobType, difficulty, sacrifice)

                    sender.sendMessage("$prefix §7セッションの開始処理を行っています...")

                    activeSession += session
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

                if (stageType == Launcher.StageType.QUICK) {

                    val session = QuickArena(uuid, players)

                    activeSession += session
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

                sender.sendMessage("$prefix §7セッションの開始処理を行っています...")

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

            for (index in activeSession.indices) {
                val session = activeSession.toList()[index]
                val type = when (session) {
                    is NormalArena -> "§cNormal"
                    is QuickArena -> "§eQuick"
                    else -> "§cERROR"
                }
                val material = when (session) {
                    is NormalArena -> Material.GREEN_CONCRETE
                    is QuickArena -> Material.YELLOW_CONCRETE
                    else -> Material.BEDROCK
                }

                val players: MutableList<String> = mutableListOf()
                Bukkit.getWorld("arena_session.${session.uuid}")!!.players.forEach{
                    players.add(it.displayName().toString())
                }

                val icon = ItemStack(material)
                val iconMeta = icon.itemMeta
                iconMeta.setItemName("§e${session.uuid}")
                iconMeta.lore = listOf(
                    "§7- $type Arena",
                    "§7- §fPlayers: §b${players.joinToString()}}",
                    "",
                    "§cShift + ホイールクリックでセッションを停止します。",
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
