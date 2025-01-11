package me.awabi2048.pve_arena.menu_manager.entrance
//
//import me.awabi2048.pve_arena.misc.MobType
//import org.bukkit.Material
//import org.bukkit.entity.Player
//import org.bukkit.event.inventory.InventoryClickEvent
//
//fun entranceMenuClicked(event: InventoryClickEvent) {
//    val player = event.whoClicked as Player
//    val slot = event.slot
//
//    // 現状を把握
//    val uuid = event.whoClicked.uniqueId.toString()
//    val mobType = MobType().toName(event.inventory.getItem(19)!!.amount - 1)
//    val difficulty = event.inventory.getItem(21)!!.amount - 1
//    val playerCount = event.inventory.getItem(23)!!.amount - 1
//
//    val retryFunction = if (event.inventory.getItem(25)!!.type == Material.CHEST_MINECART) {
//        1
//    } else {
//        0
//    }
//
//    // クリックに応じて進む・戻る
//    val delta =
//        if (event.isLeftClick) {
//            1
//        } else if (event.isRightClick) {
//            -1
//        } else {
//            0
//        }
//
//    // モブ種類
//    if (slot == 19) {
//        val mobTypeIcon = rotateOption(
//            mobType,
//            delta,
//            listOf(
//                Pair("§aゾンビ", Material.ROTTEN_FLESH),
//                Pair("§aスケルトン", Material.BONE),
//                Pair("§eクモ", Material.SPIDER_EYE),
//                Pair("§eブレイズ", Material.BLAZE_POWDER),
//                Pair("§eスライム", Material.SLIME_BALL),
//                Pair("§cガーディアン", Material.PRISMARINE_SHARD),
//                Pair("§cエンダーマン", Material.ENDER_PEARL),
//            )
//        )
//
//        event.inventory.setItem(19, mobTypeIcon)
//    }
//
//    // 難易度
//    if (slot == 21) {
//        val difficultyIcon = rotateOption(
//            difficulty,
//            delta,
//            listOf(
//                Pair("§aイージー", Material.MOSS_BLOCK),
//                Pair("§eノーマル", Material.COBBLED_DEEPSLATE),
//                Pair("§cハード", Material.MAGMA_BLOCK),
//                Pair("§dExハード", Material.CRYING_OBSIDIAN),
//            )
//        )
//
//        event.inventory.setItem(21, difficultyIcon)
//    }
//
//    // 人数
//    if (slot == 23) {
//        val playerCountIcon = rotateOption(
//            playerCount,
//            delta,
//            listOf(
//                Pair("§e1人", Material.PLAYER_HEAD),
//                Pair("§e2人", Material.PLAYER_HEAD),
//                Pair("§e3人", Material.PLAYER_HEAD),
//                Pair("§e4人", Material.PLAYER_HEAD),
//                Pair("§e5人", Material.PLAYER_HEAD),
//                Pair("§e6人", Material.PLAYER_HEAD),
//            )
//        )
//
//        event.inventory.setItem(23, playerCountIcon)
//    }
//
//    // 再入場On/Off
//    if (slot == 25) {
//        val retryFunctionIcon = rotateOption(
//            retryFunction,
//            delta,
//            listOf(
//                Pair("§cOff", Material.MINECART),
//                Pair("§aOn", Material.CHEST_MINECART),
//            )
//        )
//
//        event.inventory.setItem(25, retryFunctionIcon)
//    }
//
//    // 入場
//    if (slot == 40) {
//        val a = EntrancePortal(uuid).open(mobType)
//    }
//
//}
