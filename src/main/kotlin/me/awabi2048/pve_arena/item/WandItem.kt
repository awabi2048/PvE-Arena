package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.roundToInt

object WandItem: ItemManager() {
    override fun get(itemKind: ArenaItem): ItemStack {
        when(itemKind) {
            ArenaItem.WOODEN_WAND -> {
                val item = ItemStack(Material.WOODEN_HOE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§a木の杖")
                itemMeta.lore = listOf(
                    "§7そのあたりの木の枝で作った杖。",
                )

                itemMeta.setWandData(6.0, 10, itemKind)
                item.itemMeta = itemMeta

                return item
            }
            ArenaItem.CRYSTAL_WAND -> {
                val item = ItemStack(Material.DIAMOND_HOE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§b水晶の杖")
                itemMeta.lore = listOf(
                    "§7良質な水晶で作られた杖。",
                )

                itemMeta.setWandData(6.0, 10, itemKind)
                item.itemMeta = itemMeta

                return item
            }
            ArenaItem.MOON_STONE_WAND -> {
                val item = ItemStack(Material.STONE_HOE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§d月光石の杖")
                itemMeta.lore = listOf(
                    "§7月光石で作った杖。仮",
                )

                itemMeta.setWandData(6.0, 10, itemKind)
                item.itemMeta = itemMeta

                return item
            }
            ArenaItem.WISDOM_OAK_WAND -> {
                val item = ItemStack(Material.WOODEN_HOE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§a知恵の木の杖")
                itemMeta.lore = listOf(
                    "§7知恵の木で作った杖。仮",
                )

                itemMeta.setWandData(6.0, 10, itemKind)
                item.itemMeta = itemMeta

                return item
            }
            ArenaItem.STEADY_WOODEN_WAND -> {
                val item = ItemStack(Material.WOODEN_HOE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§a頑丈の木の杖")
                itemMeta.lore = listOf(
                    "§7頑丈の木の枝で作った杖。仮",
                )

                itemMeta.setWandData(6.0, 10, itemKind)
                item.itemMeta = itemMeta


                return item
            }
            ArenaItem.DRAGON_BORN_WAND -> {
                val item = ItemStack(Material.STONE_HOE)
                val itemMeta = item.itemMeta

                itemMeta.setItemName("§dドラゴンの骨の杖")
                itemMeta.lore = listOf(
                    "§7ドラゴンの骨で作った杖。仮",
                )

                itemMeta.setWandData(15.0, 12, itemKind)
                item.itemMeta = itemMeta

                return item
            }
            else -> throw IllegalArgumentException("@item/WandItem.kt: invalid itemKind specified.")
        }
    }

    private fun ItemMeta.setWandData(damage: Double, attackCooldown: Int, id: ArenaItem) {
        fun attackSpeedToCooldown(attackSpeed: Double): Int {
            return (20 / attackSpeed).toInt()
        }

        fun cooldownToAttackSpeed(cooldown: Int): Double {
            return 20.0 / cooldown
        }

        setMaxStackSize(1)
        addItemFlags(ItemFlag.HIDE_ATTRIBUTES)

        lore!!.plus(listOf(
            "",
            "§4${damage.roundToInt()} 攻撃力",
            "§4${(cooldownToAttackSpeed(attackCooldown) * 10).roundToInt() / 10} 攻撃速度",
        ))

        // attribute
        addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier(UUID.randomUUID().toString(), damage, AttributeModifier.Operation.ADD_NUMBER))
        addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, AttributeModifier(UUID.randomUUID().toString(), cooldownToAttackSpeed(attackCooldown) - 4.0, AttributeModifier.Operation.ADD_NUMBER))

        // id
        persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, id.name.substringAfter("ArenaItem."))

    }

    override val list: List<ArenaItem>
        get() = listOf(
            ArenaItem.WOODEN_WAND,
            ArenaItem.WISDOM_OAK_WAND,
            ArenaItem.CRYSTAL_WAND,
            ArenaItem.DRAGON_BORN_WAND,
            ArenaItem.STEADY_WOODEN_WAND,
            ArenaItem.MOON_STONE_WAND,
        )
}
