package me.awabi2048.pve_arena.menu_manager

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.game.WaveProcessingMode
import me.awabi2048.pve_arena.item.ItemManager
import me.awabi2048.pve_arena.item.ItemManager.ArenaItem.*
import me.awabi2048.pve_arena.item.KeyItem
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

class EntranceMenu(player: Player) : MenuManager(player, MenuType.Entrance) {

    override fun open() {
        val menu = structMenu(WaveProcessingMode.MobType.entries[0], WaveProcessingMode.MobDifficulty.entries[0], 1, 0)

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

            println("index:$index, cycled: $indexCycled, cur: $currentOption")

            // refresh
            val goIcon = getGoIcon(
                WaveProcessingMode.MobType.entries[index],
                getDifficultyFromIcon(menu.getItem(21)!!),
                menu.getItem(23)!!.amount,
                getSacrificeAmountFromIcon(menu.getItem(25)!!)
            )

            menu.setItem(19, getMobTypeIcon(WaveProcessingMode.MobType.entries[index]))
            menu.setItem(40, goIcon)
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
            index = min(min(indexCycled, 0), WaveProcessingMode.MobDifficulty.entries.size - 1)

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
            val changedValue = min(min(uncheckedValue, 1), DataFile.config.getInt("misc.game.player_max"))

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

    private fun structMenu(
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
        val goIcon = getGoIcon(mobType, difficulty, playerCount, sacrificeAmount)


        menu.setItem(19, mobTypeIcon)
        menu.setItem(21, difficultyIcon)
        menu.setItem(23, playersIcon)
        menu.setItem(25, sacrificeIcon)

        menu.setItem(4, topIcon)
        menu.setItem(40, goIcon)

        return menu
    }

    fun getFinalCost(menu: Inventory): Map<ItemManager.ArenaItem, Int> {
        val mobType = getMobTypeFromIcon(menu.getItem(19)!!)
        val difficulty = getDifficultyFromIcon(menu.getItem(21)!!)
        val playerCount = menu.getItem(23)!!.amount

        val finalRawCost = (calcRawCost(mobType, difficulty, playerCount) * getDiscount()).roundToInt()
        return calcCostItemMap(finalRawCost)
    }

    private fun calcRawCost(
        mobType: WaveProcessingMode.MobType,
        difficulty: WaveProcessingMode.MobDifficulty,
        playerCount: Int,
    ): Int {
        // raw cost calculation
        val baseCost = DataFile.mobType.getInt("${mobType.toString().substringAfter("MobType.").lowercase()}.base_cost")
        val difficultyMultiplier = DataFile.difficulty.getDouble(
            "${
                difficulty.toString().substringAfter("MobDifficulty.").lowercase()
            }.mob_multiplier"
        )
        val playerCountMultiplier =
            playerCount.toDouble().pow(DataFile.config.getDouble("misc.game.cost_increase_per_player_pow"))

        return (baseCost.toDouble() * difficultyMultiplier * playerCountMultiplier).roundToInt()
    }

    private fun getDiscount(): Double {
        val keyItem = KeyItem.getFromItem(player.inventory.first { KeyItem.getFromItem(it) in KeyItem.set })
        return when (keyItem) {
            KEY_30 -> 0.3
            KEY_50 -> 0.5
            else -> 1.0
        }
    }

    private fun calcCostItemMap(rawCost: Int): Map<ItemManager.ArenaItem, Int> {
        // normal
        val normal = rawCost % 6

        // rare
        val rare = rawCost / 6

        return mapOf(
            ItemManager.ArenaItem.ENTER_COST_ITEM to normal,
            ItemManager.ArenaItem.ENTER_COST_ITEM_RARE to rare
        )
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

    private fun getSacrificeAmountFromIcon(icon: ItemStack): Int {
        val rawLore = icon.itemMeta!!.lore!![2]
        val amount =
            rawLore.substringAfter("§d").removeSuffix(" 個").toIntOrNull() ?: throw IllegalArgumentException("")

        return amount
    }

    private fun getMobTypeIcon(mobType: WaveProcessingMode.MobType): ItemStack {
        fun getMobTypeIconMaterial(mobType: WaveProcessingMode.MobType): Material {
            val iconString =
                DataFile.mobType.getString("${mobType.toString().substringAfter("MobType.").lowercase()}.icon")!!
            val iconMaterial = Material.getMaterial(iconString) ?: throw (IllegalStateException("MATERIAL NOT EXIST"))
            return iconMaterial
        }

        fun getMobTypeLore(mobType: WaveProcessingMode.MobType): List<String> {
            val lore = mutableListOf(
                Lib.getBar(50, "§7"),
                "§f左クリック§7: §a次へ, §f右クリック§7: §c前へ",
                "",
                Lib.getBar(50, "§7")
            )
            for (key in DataFile.mobType.getKeys(false)) {
                val addValue =
                    if (key == mobType.toString().substringAfter("MobType.").lowercase()) {
                        "§6» §6§l${DataFile.mobType.getString("$key.name").toString()} §6«"
                    } else {
                        "§7${DataFile.mobType.getString("$key.name").toString()}"
                    }

                lore += addValue
            }

            lore += Lib.getBar(50, "§7")

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
                DataFile.difficulty.getString(
                    "${
                        difficulty.toString().substringAfter("MobDifficulty.").lowercase()
                    }.icon"
                )!!
            val iconMaterial = Material.getMaterial(iconString) ?: throw (IllegalStateException("MATERIAL NOT EXIST"))
            return iconMaterial
        }

        fun getDifficultyLore(difficulty: WaveProcessingMode.MobDifficulty): List<String> {
            val lore = mutableListOf(
                Lib.getBar(50, "§7"),
                "§f左クリック§7: §a次へ, §f右クリック§7: §c前へ",
                "",
                Lib.getBar(50, "§7")
            )
            for (key in DataFile.difficulty.getKeys(false)) {
                val addValue =
                    if (key == difficulty.toString().substringAfter("MobDifficulty.").lowercase()) {
                        "§6» §6${DataFile.difficulty.getString("$key.name").toString()} §6«"
                    } else {
                        "§7${DataFile.mobType.getString("$key.name").toString()}"
                    }

                lore += addValue
            }

            lore += Lib.getBar(50, "§7")

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
            Lib.getBar(50, "§7"),
            "§7プレイする人数を設定します。",
            "§7現在の設定§7: §6${playerCount} 人",
            Lib.getBar(50, "§7"),
        )

        item.itemMeta = itemMeta
        return item
    }

    private fun getSacrificeIcon(sacrificeAmount: Int): ItemStack {
        val item = ItemStack(Material.SOUL_CAMPFIRE, 1)
        val itemMeta = item.itemMeta

        itemMeta.setItemName("§d捧げ物")
        itemMeta.lore = listOf(
            Lib.getBar(50, "§7"),
            "§7§o何を捧げる？",
            "§e現在の設定§7: §d${sacrificeAmount} 個",
            Lib.getBar(50, "§7"),
        )

        if (sacrificeAmount >= 1) itemMeta.setEnchantmentGlintOverride(true)

        item.itemMeta = itemMeta
        return item
    }

    private fun getGoIcon(
        mobType: WaveProcessingMode.MobType,
        difficulty: WaveProcessingMode.MobDifficulty,
        playerCount: Int,
        sacrificeAmount: Int,
    ): ItemStack {
        val costDisplay: MutableList<String> = mutableListOf()
        val rawCost = calcRawCost(mobType, difficulty, playerCount)

        var keyItem: ItemStack? = null

        // cost discount calculation
        if (player.inventory.any { it != null && KeyItem.getFromItem(it) in KeyItem.set }) {
            val key = KeyItem.getFromItem(player.inventory.first { it != null &&  KeyItem.getFromItem(it) in KeyItem.set })

            val discount = when (key) {
                KEY_30 -> 0.3
                KEY_50 -> 0.5
                else -> 1.0
            }

            val trueRawCost = (rawCost * discount).roundToInt()

            // construct lore part
            costDisplay.add(
                "§7- §3ソウルフラグメント §c§m×${calcCostItemMap(rawCost)[ENTER_COST_ITEM]} §7→ §e×${
                    calcCostItemMap(
                        trueRawCost
                    )[ENTER_COST_ITEM_RARE]
                }"
            )

            if (calcCostItemMap(trueRawCost)[ENTER_COST_ITEM_RARE]!! >= 1) costDisplay.addFirst(
                "§7- §3§lソウルエッセンス §c§m×${
                    calcCostItemMap(
                        rawCost
                    )[ENTER_COST_ITEM_RARE]
                } §7→ §e×${calcCostItemMap(trueRawCost)[ENTER_COST_ITEM_RARE]}"
            )
            if (sacrificeAmount >= 1) costDisplay.add("§7- §d魂入りの瓶 §e×$sacrificeAmount")
            costDisplay.add("§7- §c§nインベントリ内のアリーナの鍵を消耗します！")

        } else {
            costDisplay.add("§7- §3ソウルフラグメント §e×${calcCostItemMap(rawCost)[ENTER_COST_ITEM]}")

            if (calcCostItemMap(rawCost)[ENTER_COST_ITEM_RARE]!! >= 1) costDisplay.addFirst(
                "§7- §3§lソウルエッセンス §e×${
                    calcCostItemMap(
                        rawCost
                    )[ENTER_COST_ITEM_RARE]
                }"
            )
            if (sacrificeAmount >= 1) costDisplay.add("§7- §d魂入りの瓶 §e×$sacrificeAmount")
        }

        val goIcon = ItemStack(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE)
        val goIconMeta = goIcon.itemMeta
        goIconMeta.setItemName("§cゲートを開く")
        goIconMeta.lore = listOf(
            Lib.getBar(50, "§7"),
            "§7クリックして§cアリーナへのゲート§7を開きます。",
            Lib.getBar(50, "§7"),
            "§d✵ コスト §7»"
        ) + costDisplay + listOf(
            Lib.getBar(50, "§7")
        )
        goIconMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        goIcon.itemMeta = goIconMeta

        return goIcon
    }

}
