package me.awabi2048.pve_arena.menu_manager.entrance
//
//import me.awabi2048.pve_arena.misc.ArenaSession
//import me.awabi2048.pve_arena.misc.MobType
//import me.awabi2048.pve_arena.misc.StageDifficulty
//import me.awabi2048.pve_arena.misc.StageType
//import org.bukkit.Bukkit.getWorld
//import org.bukkit.Location
//import org.bukkit.Sound
//import org.bukkit.entity.Entity
//import org.bukkit.entity.Player
//
//class EntrancePortal(private val uuid: String) {
//    fun open(mobType: MobType, difficulty: StageDifficulty, stageType: StageType, portalEntity: Entity): Boolean {
//
//        // セッションデータを作成
//        ArenaSession.register(uuid, mobType, difficulty, stageType, portalEntity)
//
//        // セッションのワールドを作成
//        val sessionWorldPreparationSucceeded = ArenaSession.prepareWorld(uuid)
//        if (!sessionWorldPreparationSucceeded) return false
//
//        // ポータル開通アニメーションを再生
//
//
//        return true
//    }
//
//    fun close(): Boolean {
//        return true
//    }
//
//    fun transportPlayer(player: Player): Boolean {
//        val world = getWorld("arena_session.$uuid")?: return false
//
//        // tp
//        player.teleport(Location(world, 0.0, 0.0, 0.0))
//
//        // 効果音
//        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.75f)
//
//        return true
//    }
//}
