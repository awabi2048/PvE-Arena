package me.awabi2048.pve_arena.command

import me.awabi2048.pve_arena.Main.Companion.activeSession
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.command.SubCommandAdmin.Option.*
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.config.YamlUtil
import me.awabi2048.pve_arena.game.*
import me.awabi2048.pve_arena.item.*
import me.awabi2048.pve_arena.misc.Lib
import me.awabi2048.pve_arena.misc.sendError
import me.awabi2048.pve_arena.quest.DailyQuest
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SubCommandAdmin(private val sender: Player, private val args: Array<out String>, private val option: Option) {
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

            val type = Launcher.Type.valueOf(args[1])
//
                val uuid = sender.uniqueId.toString()
                val players = setOf(sender)

            if (type == Launcher.Type.DUNGEON) {
                val session = ArenaDungeon(
                    uuid = uuid,
                    players = players,
                    status = ArenaDungeon.Status.WaitingGeneration,
                    structureType = ArenaDungeon.StructureType.DESERT_TEMPLE
                )

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

//            try {
//                sender.sendMessage("$prefix §7開始処理を実行中です。通常§n2秒程度§7掛かります。")
//
//                val type = Launcher.Type.valueOf(args[1])
//
//                val uuid = sender.uniqueId.toString()
//                val players = setOf(sender)
//
//                if (type == Launcher.Type.NORMAL) {
//                    val mobType = WaveProcessingMode.MobType.valueOf(args[2])
//                    val difficulty = WaveProcessingMode.MobDifficulty.valueOf(args[3])
//                    val sacrifice = args[4].toInt()
//
////                    val initialStatus = NormalArena.Status(0, players.toMutableSet(), Generic.StatusCode.WAITING_GENERATION)
//                    val session = NormalArena(uuid, players, mobType, difficulty, sacrifice)
//
//                    activeSession += session
//                    session.generate()
//
//                    Bukkit.getScheduler().runTaskLater(
//                        instance,
//                        Runnable {
//                            session.joinPlayer(sender)
//                            session.start()
//                        },
//                        40L
//                    )
//                }
//
//                if (type == Launcher.Type.QUICK) {
//
//                    val session = QuickArena(uuid, players)
//
//                    activeSession += session
//                    session.generate()
//
//                    Bukkit.getScheduler().runTaskLater(
//                        instance,
//                        Runnable {
//                            session.joinPlayer(sender)
//                            session.start()
//                        },
//                        40L
//                    )
//                }
//
//                if (type == Launcher.Type.DUNGEON) {
//                    val session = ArenaDungeon(
//                        uuid = uuid,
//                        players = players,
//                        status = ArenaDungeon.Status.WaitingGeneration,
//                        structureType = ArenaDungeon.StructureType.DESERT_TEMPLE
//                    )
//
//                    activeSession += session
//                    session.generate()
//
//                    Bukkit.getScheduler().runTaskLater(
//                        instance,
//                        Runnable {
//                            session.joinPlayer(sender)
//                            session.start()
//                        },
//                        40L
//                    )
//                }
//
//            } catch (e: Exception) {
//                sender.sendError("無効なセッション情報です: start_session <TYPE> <MobType> <Difficulty> <Sacrifice>")
//            }
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
                Bukkit.getWorld("arena_session.${session.uuid}")!!.players.forEach {
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

        fun getItem() {
            try {
                val itemId = args[1].uppercase()
                val item = when (val itemKind = ItemManager.ArenaItem.valueOf(itemId)) {
                    in AccessoryItem.list -> AccessoryItem.get(itemKind)
                    in BoosterItem.list -> BoosterItem.get(itemKind)
                    in BowItem.list -> BowItem.get(itemKind)
                    in EnchantmentItem.list -> EnchantmentItem.get(itemKind)
                    in EnterCostItem.list -> EnterCostItem.get(itemKind)
                    in KeyItem.list -> KeyItem.get(itemKind)
                    in SacrificeItem.list -> SacrificeItem.get(itemKind)
                    in SwordItem.list -> SwordItem.get(itemKind)
                    in TicketItem.list -> TicketItem.get(itemKind)
                    in WandItem.list -> WandItem.get(itemKind)
                    else -> throw IllegalArgumentException()
                }

                sender.inventory.addItem(item)

            } catch (e: Exception) {
                sender.sendError("無効なアイテムIdです。")
            }
        }

        fun questUpdate() {
            DailyQuest.update()
        }

        fun changeJob() {
            if (args[1] !in listOf("SWORDSMAN", "ARCHER", "MAGE")) {
                sender.sendError("無効なコマンドです。")
                return
            }

            sender.sendMessage("$prefix §7職種を${args[1]}に変更しました。")

            DataFile.playerData.set("${sender.uniqueId}.profession", args[1])
            YamlUtil.save("player_data/main.yml", DataFile.playerData)
        }

        when (option) {
            CONFIG -> config()
            START_SESSION -> startSession()
            JOIN_SESSION -> TODO()
            STOP_SESSION -> stopSession()
            GET_ITEM -> getItem()
            QUEST_UPDATE -> questUpdate()
            CHANGE_JOB -> changeJob()
        }
    }

    enum class Option {
        CONFIG,
        START_SESSION,
        JOIN_SESSION,
        STOP_SESSION,
        GET_ITEM,
        QUEST_UPDATE,
        CHANGE_JOB
    }
}
