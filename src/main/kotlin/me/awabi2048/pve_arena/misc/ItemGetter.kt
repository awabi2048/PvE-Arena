package me.awabi2048.pve_arena.misc

import org.bukkit.Bukkit.getWorld
import org.bukkit.block.Barrel
import org.bukkit.inventory.ItemStack

fun getItemWithAddress(address: String): ItemStack? {
    // アドレスを分離
    val addressX = address.substringBefore(",", "-1").toInt()
    val addressZ = address.substringAfter(",", "-1").toInt()
    val addressSlot = address.substringBeforeLast(",", "-1").toInt()

    // アドレスにあるコンテナを取得
    val addressContainer = getWorld("minecraft:arena")?.getBlockAt(addressX, 0, addressZ) as Barrel??: return null

    // アイテムの取得
    val addressedItem = addressContainer.inventory.getItem(addressSlot)?: return null

    // return
    return addressedItem
}
