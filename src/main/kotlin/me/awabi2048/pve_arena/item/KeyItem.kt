package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object KeyItem : ItemManager() {
    val set = setOf(
        ArenaItem.KEY_30,
        ArenaItem.KEY_50,
    )

    override fun get(itemKind: ArenaItem): ItemStack {
        when (itemKind) {
            ArenaItem.KEY_30 -> {
                val item = ItemStack(Material.TRIAL_KEY)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§cぼろぼろのアリーナゲートの鍵")
                itemMeta.lore = listOf(
                    "§7アリーナ入場時に耐久値を消費し、コストを§630%§7削減します。",
                    "",
                    Lib.getBar(50, "§8"),
                    "§7残り耐久値: §a10§7/10",
                    Lib.getBar(50, "§8"),
                )
                itemMeta.setMaxStackSize(1)
                itemMeta.setEnchantmentGlintOverride(false)
                (itemMeta as Damageable).setMaxDamage(10)
                itemMeta.damage = 0

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, "KEY_30")

                item.itemMeta = itemMeta
                return item
            }
            ArenaItem.KEY_50 -> {
                val item = ItemStack(Material.TRIAL_KEY)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§c欠けたアリーナゲートの鍵")
                itemMeta.lore = listOf(
                    "§7アリーナ入場時に耐久値を消費し、コストを§650%§7削減します。",
                    "",
                    Lib.getBar(50, "§8"),
                    "§7残り耐久値: §a20§7/20",
                    Lib.getBar(50, "§8"),
                )
                itemMeta.setMaxStackSize(1)
                itemMeta.setEnchantmentGlintOverride(false)
                (itemMeta as Damageable).setMaxDamage(20)
                itemMeta.damage = 0

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, "KEY_50")

                item.itemMeta = itemMeta
                return item
            }
            else -> throw IllegalArgumentException("@item/KeyItem.kt: invalid itemKind specified.")
        }
    }

    fun onUse() {

    }
}
