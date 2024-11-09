package me.awabi2048.kota_arena.menu_manager.entrance

import me.awabi2048.kota_arena.session_manager.sessionWorldPreparation
import java.util.*

class EntrancePortal {
    var uuid: String = ""

    fun open(): Boolean {

        // セッションデータを作成


        // セッションのワールドを作成
        val sessionWorldPreparationSucceeded = sessionWorldPreparation(uuid)
        if (!sessionWorldPreparationSucceeded) return false

        // ポータル開通アニメーションを再生

        return true
    }

    fun close(): Boolean {
        return true
    }
}
