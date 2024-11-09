package me.awabi2048.kota_arena.misc

import me.awabi2048.kota_arena.config.YamlUtil
import org.bukkit.entity.Mob
import kotlin.enums.enumEntries

enum class MobType(val index: Int, val displayName: String) {
    ZOMBIE(0, "§aゾンビ"),
    SKELETON(1, "§aスケルトン"),
    SPIDER(2, "§eクモ"),
    BLAZE(3, "§eブレイズ"),
    SLIME(4, "§cスライム"),
    GUARDIAN(5, "§cガーディアン"),
    ENDERMAN(6, "§cエンダーマン");

    // indexからMobTypeへの変換
    fun toName(index: Int): String {
        for (item in enumValues<MobType>()) {
            if (item.index == index) {
                return item.displayName
            }
        }
        return "§cMobTypeConversionFailed"
    }
}

enum class StageDifficulty(val index: Int, val displayName: String) {
    EASY(0, "§aイージー"),
    NORMAL(1, "§eノーマル"),
    HARD(2, "§cハード"),
    MASTER(3, "§4マスター"),
}

enum class StageType {
    NORMAL,
    QUICK,
    DUNGEON
}
