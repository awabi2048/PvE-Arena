package me.awabi2048.pve_arena.game

import javax.annotation.Nullable

sealed class GameType {
    data class Normal(@Nullable val mobType: WaveProcessingMode.MobType?, @Nullable val mobDifficulty: WaveProcessingMode.MobDifficulty?): GameType()
    data object Quick: GameType()
    data object Dungeon: GameType()
    data object Boss: GameType()
}
