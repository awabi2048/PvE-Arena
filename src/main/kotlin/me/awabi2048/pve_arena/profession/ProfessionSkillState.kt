package me.awabi2048.pve_arena.profession

data class ProfessionSkillState(var expireTimer: Int, val spell: MutableList<SpellClick>) {
    enum class SpellClick {
        RIGHT,
        LEFT;
    }
}

