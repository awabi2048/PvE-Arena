package me.awabi2048.pve_arena.menu

import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

class EnchantingMenu(player: Player) : MenuManager(player, MenuType.Enchanting) {
    val isPossibleInfusion: Boolean
        get() {
            if (player.openInventory.getItem(11) == null || player.openInventory.getItem(15) == null) return false
            return simulateInfusion(player.openInventory.getItem(11)!!, player.openInventory.getItem(15)!!) != null
        }
    val infusionResult: ItemStack?
        get() {
            if (player.openInventory.getItem(11) == null || player.openInventory.getItem(15) == null) return null
            return simulateInfusion(player.openInventory.getItem(11)!!, player.openInventory.getItem(15)!!)
        }

    val cost = calcCost(player.openInventory.getItem(11)!!, player.openInventory.getItem(15)!!)

    override fun open() {
        val menu = Bukkit.createInventory(null, 45, "§8§lArena Enchanting")

        val white = Lib.getHiddenItem(Material.WHITE_STAINED_GLASS_PANE)

        for (row in 0..8) {
            menu.setItem(row, black)
            menu.setItem(row + 9, gray)
            menu.setItem(row + 18, gray)
            menu.setItem(row + 27, gray)
            menu.setItem(row + 36, black)
        }

        // ingredient
        val ingredientIcon = ItemStack(Material.LIGHT_GRAY_STAINED_GLASS)
        val ingredientIconMeta = ingredientIcon.itemMeta
        ingredientIconMeta.setItemName("§7空のスロット")
        ingredientIconMeta.lore = listOf(
            "§fクリックしてスロットにアイテムを配置します。"
        )
        ingredientIcon.itemMeta = ingredientIconMeta

        // product icon
        val productIcon = Lib.getHiddenItem(Material.BLACK_STAINED_GLASS)

        // deco
        val decorationItem = Lib.getHiddenItem(Material.WHITE_STAINED_GLASS_PANE)
        for (slot in listOf(20, 24, 29, 30, 32, 33)) {
            menu.setItem(slot, white)
        }

        // 
        val infusionIcon = ItemStack(Material.DAMAGED_ANVIL)
        val infusionIconMeta = infusionIcon.itemMeta
        infusionIconMeta.setItemName("§c合成する")
        infusionIconMeta.lore = listOf(
            Lib.getBar(40, "§7"),
            "§bコスト: ",
            "§c合成不可",
            Lib.getBar(40, "§7"),
        )
        infusionIcon.itemMeta = infusionIconMeta

        // place
        menu.setItem(11, ingredientIcon)
        menu.setItem(15, ingredientIcon)

        menu.setItem(31, productIcon)
        menu.setItem(40, infusionIcon)

        player.openInventory(menu)

        player.playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.75f)
    }

    private fun calcCost(baseItem: ItemStack, bookItem: ItemStack): Pair<Int, Int>? {
        val infusedResult = simulateInfusion(baseItem, bookItem) ?: return null
        var costItem: Int = 0
        var costExpLevel: Int = 0

        // 本&本
        if (bookItem.type == baseItem.type) {
            val infusedLevel = infusedResult.itemMeta.enchants.values.first()

            val enchantName = bookItem.itemMeta.enchants.entries.first().key.key.toString()
            val baseCost =
                DataFile.enchanting.getConfigurationSection(enchantName)?.getConfigurationSection("cost")
                    ?: throw IllegalStateException("Enchant section not found in misc/enchanting.yml: $enchantName")

            costItem = baseCost.getInt("soul") * infusedLevel
            costExpLevel = baseCost.getInt("exp_level") * infusedLevel

        } else {
            val resultItemEnchantments = infusedResult.itemMeta.enchants.entries
            for (enchantment in resultItemEnchantments) {
                val enchantmentName = enchantment.key.key().toString()
                val baseCost = DataFile.enchanting.getConfigurationSection(enchantmentName)
                    ?.getConfigurationSection("$enchantmentName.cost")
                    ?: throw IllegalStateException("Enchant section not found in misc/enchanting.yml: $enchantmentName")

                costItem += baseCost.getInt("soul") * enchantment.value
                costExpLevel += baseCost.getInt("exp_level") * enchantment.value
            }
        }

        return Pair(
            costItem, costExpLevel
        )
    }

    fun simulateInfusion(baseItem: ItemStack, bookItem: ItemStack): ItemStack? {
        // そもそも引数アイテムが有効かどうか
        if (bookItem.type != Material.ENCHANTED_BOOK) return null // 本か、本以外か
        if (baseItem.itemMeta.enchants.isEmpty()) return null // エンチャントつき
        if (bookItem.itemMeta.enchants.isEmpty()) return null // エンチャントつき
        println("${baseItem.itemMeta.enchants} AND ${bookItem.itemMeta.enchants}")
        if (!baseItem.itemMeta.enchants.any { it.key in bookItem.itemMeta.enchants.keys && it.value == bookItem.itemMeta.enchants[it.key]!! }) return null // ベースアイテムが同じ種類・レベルのエンチャントを含まない場合は例外
        if (bookItem.itemMeta.enchants.size != 1) return null // 本は1つのエンチャントのみ許可

        val bookEnchantment = bookItem.itemMeta.enchants.entries.first()

        val enchantName = bookEnchantment.key.key.toString()
        if (bookEnchantment.value + 1 !in Lib.intRangeOf(DataFile.enchanting.getString("$enchantName.level")!!)!!) return null // 合成後のレベルがサポートのレベルか判定

        println(enchantName)

        // 本+本の場合 → ベースが単品本でないなら無効
        if (baseItem.type == bookItem.type && baseItem.itemMeta.enchants.size != 1) return null

        // upgrade
        val resultItem = baseItem.clone()
        val resultItemMeta = resultItem.itemMeta
        resultItemMeta.addEnchant(bookEnchantment.key, bookEnchantment.value + 1, true)
        resultItem.itemMeta = resultItemMeta

        return resultItem
    }
}
