package me.awabi2048.kota_arena.menu_manager.entrance

import me.awabi2048.kota_arena.Main.Companion.preSessionData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

fun getEntranceMenu(player: Player, uuid: String): Inventory? {
//    // どういうわけか権限がない
//    if (player.hasPermission(Permission("kota_arena.menu"))) {
//        return null
//    }

    val menu = Bukkit.createInventory(null, 45, "§4Arena")

    //

    // オプション: モブ種類
    val mobTypeIcon = rotateOption(
        0,
        0,
        listOf(
            Pair("§aゾンビ", Material.ROTTEN_FLESH),
            Pair("§aスケルトン", Material.BONE),
            Pair("§eクモ", Material.SPIDER_EYE),
            Pair("§eブレイズ", Material.BLAZE_POWDER),
            Pair("§eスライム", Material.SLIME_BALL),
            Pair("§cガーディアン", Material.PRISMARINE_SHARD),
            Pair("§cエンダーマン", Material.ENDER_PEARL),
        )
    )

    menu.setItem(19, mobTypeIcon)

    // オプション: 難易度
    val difficultyIcon = rotateOption(
        0,
        0,
        listOf(
            Pair("§aイージー", Material.MOSS_BLOCK),
            Pair("§eノーマル", Material.COBBLED_DEEPSLATE),
            Pair("§cハード", Material.MAGMA_BLOCK),
            Pair("§dExハード", Material.CRYING_OBSIDIAN),
        )
    )

    menu.setItem(21, difficultyIcon)

    // オプション: 人数
    val playerCountIcon = rotateOption(
        0,
        0,
        listOf(
            Pair("§e1人", Material.PLAYER_HEAD),
            Pair("§e2人", Material.PLAYER_HEAD),
            Pair("§e3人", Material.PLAYER_HEAD),
            Pair("§e4人", Material.PLAYER_HEAD),
            Pair("§e5人", Material.PLAYER_HEAD),
            Pair("§e6人", Material.PLAYER_HEAD),
        )
    )

    menu.setItem(23, playerCountIcon)

    // オプション: 再入場 On/Off
    val retryFunctionIcon = rotateOption(
        preSessionData[uuid]!!,
        0,
        listOf(
            Pair("§cOff", Material.MINECART),
            Pair("§aOn", Material.CHEST_MINECART),
        )
    )

    menu.setItem(25, retryFunctionIcon)

    // 入場ボタン
    val confirmIcon = ItemStack(Material.OMINOUS_TRIAL_KEY)
    val confirmIconMeta = confirmIcon.itemMeta
    confirmIconMeta.setItemName("§a【ゲートを開く】")
    confirmIconMeta.lore = listOf()


    return menu

}
