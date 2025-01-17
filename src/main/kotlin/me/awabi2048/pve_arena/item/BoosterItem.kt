package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object BoosterItem: ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        when(itemKind) {
            ArenaItem.EXP_BOOST_100 -> {
                val item = ItemStack(Material.EXPERIENCE_BOTTLE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§aアリーナ経験値ブースター")
                itemMeta.lore = listOf(
                    "§b30分間§7、獲得するアリーナ経験値を§a100%ブースト§7します！",
                    "§eサーバー全体§7に効果があります。§6右クリックで使用§7します。",
                )
                itemMeta.setMaxStackSize(1)
                itemMeta.setEnchantmentGlintOverride(false)

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, itemKind.name.substringAfter("ArenaItem."))

                item.itemMeta = itemMeta
                return item
            }
            ArenaItem.EXP_BOOST_200 -> {
                val item = ItemStack(Material.EXPERIENCE_BOTTLE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§dアリーナ経験値ブースター")
                itemMeta.lore = listOf(
                    "§b30分間§7、獲得するアリーナ経験値を§a§l200%ブースト§7します！",
                    "§eサーバー全体§7に効果があります。§6右クリックで使用§7します。",
                )
                itemMeta.setMaxStackSize(1)
                itemMeta.setEnchantmentGlintOverride(true)

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, itemKind.name.substringAfter("ArenaItem."))

                item.itemMeta = itemMeta
                return item
            }
            ArenaItem.REWARD_BOOST_30 -> {
                val item = ItemStack(Material.COOKIE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§6アリーナ報酬ブースター")
                itemMeta.lore = listOf(
                    "§b30分間§7、獲得するアリーナ報酬を§a30%§7ブーストします！",
                    "§eサーバー全体§7に効果があります。§6右クリックで使用§7します。",
                    Lib.getBar(40, "§8"),
                    "§7効果を受ける報酬 »",
                    "§7- アリーナチケット",
                    "§7- アリーナポイント",
                    Lib.getBar(40, "§8"),
                )
                itemMeta.setMaxStackSize(1)
                itemMeta.setEnchantmentGlintOverride(false)

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, itemKind.name.substringAfter("ArenaItem."))

                item.itemMeta = itemMeta
                return item
            }
            ArenaItem.REWARD_BOOST_50 -> {
                val item = ItemStack(Material.COOKIE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§dアリーナ報酬ブースター")
                itemMeta.lore = listOf(
                    "§b§n30分間§7、獲得するアリーナ報酬を§a§n50%§7ブーストします！",
                    "§eサーバー全体§7に効果があります。§6右クリックで使用§7します。",
                    Lib.getBar(30, "§8"),
                    "§7§n効果を受ける報酬 »",
                    "§7- §fアリーナチケット",
                    "§7- §fアリーナポイント",
                    Lib.getBar(30, "§8"),
                )
                itemMeta.setMaxStackSize(1)
                itemMeta.setEnchantmentGlintOverride(true)

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, itemKind.name.substringAfter("ArenaItem."))

                item.itemMeta = itemMeta
                return item
            }
            else -> throw IllegalArgumentException("@item/BoosterItem.kt: invalid itemKind specified.")
        }
    }

    override val list = listOf(
        ArenaItem.EXP_BOOST_100,
        ArenaItem.EXP_BOOST_200,
        ArenaItem.REWARD_BOOST_30,
        ArenaItem.REWARD_BOOST_50,
    )

    fun onUse(item: ItemStack, user: Player) {

    }
}
