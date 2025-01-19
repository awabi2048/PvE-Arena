package me.awabi2048.pve_arena.item

import me.awabi2048.pve_arena.Main.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

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

                itemMeta.setMaxStackSize(1)
                itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier(UUID.randomUUID().toString(), 3.0, AttributeModifier.Operation.ADD_NUMBER))
                itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, AttributeModifier(UUID.randomUUID().toString(), cooldownToAttackSpeed(10) - 4.0, AttributeModifier.Operation.ADD_NUMBER))

                itemMeta.persistentDataContainer.set(NamespacedKey(instance, "id"), PersistentDataType.STRING, itemKind.name.substringAfter("ArenaItem."))

                item.itemMeta = itemMeta
                return item
            }
            else -> throw IllegalArgumentException("@item/WandItem.kt: invalid itemKind specified.")
        }
    }

    fun attackSpeedToCooldown(attackSpeed: Double): Int {
        return (20 / attackSpeed).toInt()
    }

    fun cooldownToAttackSpeed(cooldown: Int): Double {
        return 20.0 / cooldown
    }

    override val list: List<ArenaItem>
        get() = listOf(
            ArenaItem.WOODEN_WAND,
        )
}
