package me.awabi2048.kota_arena.menu_manager.entrance

import jdk.internal.classfile.impl.DirectFieldBuilder
import me.awabi2048.kota_arena.misc.ArenaSession
import me.awabi2048.kota_arena.misc.MobType
import me.awabi2048.kota_arena.misc.StageDifficulty
import me.awabi2048.kota_arena.misc.StageType
import java.util.*

class EntrancePortal {
    var uuid: String = ""

    fun open(mobType: MobType, difficulty: StageDifficulty, stageType: StageType): Boolean {

        // セッションデータを作成
        ArenaSession.register(uuid, mobType, difficulty, stageType)

        // セッションのワールドを作成
        val sessionWorldPreparationSucceeded = ArenaSession.prepareWorld(uuid)
        if (!sessionWorldPreparationSucceeded) return false

        // ポータル開通アニメーションを再生


        return true
    }

    fun close(): Boolean {
        return true
    }
}
