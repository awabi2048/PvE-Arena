package me.awabi2048.pve_arena.menu_manager

import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.game.WaveProcessingMode
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.math.min

class EntranceMenu(player: Player) : MenuManager(player, MenuType.Entrance) {

    override fun open() {
        val menu = getMenu(WaveProcessingMode.MobType.entries[0], WaveProcessingMode.MobDifficulty.entries[0], 1, 0)

        player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 0.8f)
        player.playSound(player, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1.0f, 0.8f)

        player.openInventory(menu)
    }

    fun cycleOption(menu: Inventory, option: OptionCategory, inverted: Boolean) {
        if (option !in listOf(OptionCategory.MobType, OptionCategory.MobDifficulty)) return

        // initialize
        var currentOption = when (option) {
            OptionCategory.MobType -> WaveProcessingMode.MobType.ZOMBIE
            OptionCategory.MobDifficulty -> WaveProcessingMode.MobDifficulty.EASY
            else -> null
        }
        var currentOptionIndex = 0
        var index = 0

        if (option == OptionCategory.MobType) {
            // get current state
            val mobTypeIcon = menu.getItem(19)!!
            for (i in WaveProcessingMode.MobType.entries.indices) {
                val mobType = WaveProcessingMode.MobType.entries[i]

                // get material
                val iconMaterial = Material.getMaterial(
                    DataFile.mobType.getString(
                        "${
                            mobType.toString().substringAfter("MobType.").lowercase()
                        }.icon"
                    )!!
                )

                // check if match
                if (mobTypeIcon.type == iconMaterial) {
                    currentOption = mobType
                    currentOptionIndex = i
                    break
                }
            }

            // set index
            val indexCycled = when (inverted) {
                false -> currentOptionIndex + 1
                true -> currentOptionIndex - 1
            }
            index = max(min(indexCycled, 0), WaveProcessingMode.MobType.entries.size - 1)

            // replace
            menu.setItem(19, getMobTypeIcon(WaveProcessingMode.MobType.entries[index]))
        }
        if (option == OptionCategory.MobDifficulty) {
            // get current state
            val mobTypeIcon = menu.getItem(21)!!
            for (i in WaveProcessingMode.MobDifficulty.entries.indices) {
                val mobDifficulty = WaveProcessingMode.MobDifficulty.entries[i]

                // get material
                val iconMaterial = Material.getMaterial(
                    DataFile.difficulty.getString(
                        "${
                            mobDifficulty.toString().substringAfter("MobDifficulty.").lowercase()
                        }.icon"
                    )!!
                )

                // check if match
                if (mobTypeIcon.type == iconMaterial) {
                    currentOption = mobDifficulty
                    currentOptionIndex = i
                    break
                }
            }

            // set index
            val indexCycled = when (inverted) {
                false -> currentOptionIndex + 1
                true -> currentOptionIndex - 1
            }
            index = max(min(indexCycled, 0), WaveProcessingMode.MobDifficulty.entries.size - 1)

            // replace
            menu.setItem(21, getDifficultyIcon(WaveProcessingMode.MobDifficulty.entries[index]))
        }
    }

    fun changeOptionValue(menu: Inventory, option: OptionCategory, inverted: Boolean) {
        if (option !in listOf(OptionCategory.PlayerCount, OptionCategory.SacrificeAmount)) return

        // initialize
        var uncheckedValue = 0

        if (option == OptionCategory.PlayerCount) {
            // get current state
            uncheckedValue = when (inverted) {
                true -> menu.getItem(23)!!.amount - 1
                false -> menu.getItem(23)!!.amount + 1
            }

            // change value
            val changedValue = max(min(uncheckedValue, 1), DataFile.config.getInt("misc.game.player_max"))

            // refresh
            menu.setItem(23, getPlayersIcon(changedValue))
        }
        if (option == OptionCategory.SacrificeAmount) {
            // get current state
            uncheckedValue = when (inverted) {
                true -> menu.getItem(25)!!.amount - 1
                false -> menu.getItem(25)!!.amount + 1
            }

            // change value
            val changedValue = max(min(uncheckedValue, 0), DataFile.config.getInt("misc.game.sacrifice_max"))

            // refresh
            menu.setItem(25, getSacrificeIcon(changedValue))
        }
    }

    fun openGate() {

    }

    enum class OptionCategory {
        MobType,
        MobDifficulty,
        PlayerCount,
        SacrificeAmount
    }

    enum class EnterCost {
        FRAGMENT,
        ESSENCE;
    }

    private fun getMenu(
        mobType: WaveProcessingMode.MobType,
        difficulty: WaveProcessingMode.MobDifficulty,
        playerCount: Int,
        sacrificeAmount: Int,
    ): Inventory {
        val menu = Bukkit.createInventory(null, 45, "§7§lArena Entrance")

        // blank
        for (slot in 0..8) {
            menu.setItem(slot, black)
            menu.setItem(slot + 9, gray)
            menu.setItem(slot + 18, gray)
            menu.setItem(slot + 27, gray)
            menu.setItem(slot + 36, black)
        }

        // mob type
        val mobTypeIcon = getMobTypeIcon(mobType)

        // difficulty
        val difficultyIcon = getDifficultyIcon(difficulty)

        // player
        val playersIcon = getPlayersIcon(playerCount)

        // sacrifice
        val sacrificeIcon = getSacrificeIcon(sacrificeAmount)

        // top icon
        val topIcon = Lib.getHiddenItem(Material.TRIDENT)

        // go icon
        val goIcon = ItemStack(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE)
        val goIconMeta = goIcon.itemMeta
        goIconMeta.setItemName("§cゲートを開く")
        goIconMeta.lore = listOf(
            bar,
            "§7クリックして§cアリーナへのゲート§7を開きます。",
            bar,
            "§d✵ コスト §7»",
            "",
        )


        menu.setItem(19, mobTypeIcon)
        menu.setItem(21, difficultyIcon)
        menu.setItem(23, playersIcon)
        menu.setItem(25, sacrificeIcon)

        return menu
    }

    fun costCalc(type: EnterCost, menu: Inventory): Int {
        val mobType = getMobTypeFromIcon(menu.getItem(19)!!)
        val difficulty = getDifficultyFromIcon(menu.getItem(21)!!)
        val playerCount = menu.getItem(23)!!.amount

        val baseCost = DataFile.mobType.getInt("${mobType.toString().substringAfter("MobType.").lowercase()}.base_cost")


        val rawCost =
    }

    private fun getMobTypeFromIcon(icon: ItemStack): WaveProcessingMode.MobType {
        return WaveProcessingMode.MobType.entries.first {
            icon.type == Material.getMaterial(
                DataFile.mobType.getString(
                    "${
                        it.toString().substringAfter("MobType.").lowercase()
                    }.icon"
                )!!
            )
        }
    }

    private fun getDifficultyFromIcon(icon: ItemStack): WaveProcessingMode.MobDifficulty {
        return WaveProcessingMode.MobDifficulty.entries.first {
            icon.type == Material.getMaterial(
                DataFile.difficulty.getString(
                    "${
                        it.toString().substringAfter("MobDifficulty.").lowercase()
                    }.icon"
                )!!
            )
        }
    }

    private fun getMobTypeIcon(mobType: WaveProcessingMode.MobType): ItemStack {
        fun getMobTypeIconMaterial(mobType: WaveProcessingMode.MobType): Material {
            val iconString =
                DataFile.mobType.getString("${mobType.toString().substringAfter("MobType.").lowercase()}.icon")!!
            val iconMaterial = Material.getMaterial(iconString) ?: throw (IllegalStateException("MATERIAL NOT EXIST"))
            return iconMaterial
        }

        fun getMobTypeLore(mobType: WaveProcessingMode.MobType): List<String> {
            val lore = mutableListOf(bar, "§f左クリック§7: §a次へ, §f右クリック§7: §c前へ", "", bar)
            for (key in DataFile.mobType.getKeys(false)) {
                val addValue =
                    if (key == mobType.toString().substringAfter("MobType.").lowercase()) {
                        "§6» §6§l${DataFile.mobType.getString("$key.name").toString()} §6«"
                    } else {
                        "§7${DataFile.mobType.getString("$key.name").toString()}"
                    }

                lore += addValue
            }

            lore += bar

            return lore
        }

        val material = getMobTypeIconMaterial(mobType)
        val lore = getMobTypeLore(mobType)

        val item = ItemStack(material)
        val itemMeta = item.itemMeta
        itemMeta.setItemName("§aモブのタイプ選択")
        itemMeta.lore = lore

        item.itemMeta = itemMeta

        return item
    }

    private fun getDifficultyIcon(difficulty: WaveProcessingMode.MobDifficulty): ItemStack {
        fun getDifficultyIconMaterial(difficulty: WaveProcessingMode.MobDifficulty): Material {
            val iconString =
                DataFile.mobType.getString(
                    "${
                        difficulty.toString().substringAfter("MobDifficulty.").lowercase()
                    }.icon"
                )!!
            val iconMaterial = Material.getMaterial(iconString) ?: throw (IllegalStateException("MATERIAL NOT EXIST"))
            return iconMaterial
        }

        fun getDifficultyLore(difficulty: WaveProcessingMode.MobDifficulty): List<String> {
            val lore = mutableListOf(bar, "§f左クリック§7: §a次へ, §f右クリック§7: §c前へ", "", bar)
            for (key in DataFile.difficulty.getKeys(false)) {
                val addValue =
                    if (key == difficulty.toString().substringAfter("MobDifficulty.").lowercase()) {
                        "§6» §6§l${DataFile.difficulty.getString("$key.name").toString()} §6«"
                    } else {
                        "§7${DataFile.mobType.getString("$key.name").toString()}"
                    }

                lore += addValue
            }

            lore += bar

            return lore
        }

        val material = getDifficultyIconMaterial(difficulty)
        val lore = getDifficultyLore(difficulty)

        val item = ItemStack(material)
        val itemMeta = item.itemMeta
        itemMeta.setItemName("§a難易度選択")
        itemMeta.lore = lore

        item.itemMeta = itemMeta

        return item
    }

    private fun getPlayersIcon(playerCount: Int): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD, playerCount)
        val itemMeta = item.itemMeta

        itemMeta.setItemName("§aプレイ人数")
        itemMeta.lore = listOf(
            bar,
            "§7プレイする人数を設定します。",
            "§7現在の設定§7: §6${playerCount} 人",
            bar,
        )

        item.itemMeta = itemMeta
        return item
    }

    private fun getSacrificeIcon(sacrificeAmount: Int): ItemStack {
        val item = ItemStack(Material.SOUL_CAMPFIRE, 1)
        val itemMeta = item.itemMeta

        itemMeta.setItemName("§d捧げ物")
        itemMeta.lore = listOf(
            bar,
            "§7§o何を捧げる？",
            "§e現在の設定§7: §d${sacrificeAmount} 個",
            bar,
        )

        if (sacrificeAmount >= 1) itemMeta.setEnchantmentGlintOverride(true)

        item.itemMeta = itemMeta
        return item
    }

}
