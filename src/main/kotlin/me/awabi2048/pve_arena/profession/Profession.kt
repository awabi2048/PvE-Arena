package me.awabi2048.pve_arena.profession

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.playerSkillState
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.profession.PlayerProfession.*
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

abstract class Profession(private val player: Player) {
    fun spell(click: ProfessionSkillState.SpellClick) {
        if (playerSkillState[player] == null) {
            val playerProfession = PlayerProfession.getProfession(player)!!
            if (playerProfession == ARCHER) {
                if (click == ProfessionSkillState.SpellClick.RIGHT) return
            } else {
                if (click == ProfessionSkillState.SpellClick.LEFT) return
            }

            playerSkillState[player] = ProfessionSkillState(15, mutableListOf())

            // start expire timer
            object: BukkitRunnable() {
                override fun run() {
                    if (!playerSkillState.contains(player)) {
                        cancel()
                    } else {
//                    println("${playerSkillState[player]!!.expireTimer}")
                        playerSkillState[player]!!.expireTimer -= 1

                        val spell = playerSkillState[player]!!.spell

                        val a: MutableList<String> = mutableListOf()
                        for (s in spell) {
                            if (s == ProfessionSkillState.SpellClick.RIGHT) a += "§6R"
                            if (s == ProfessionSkillState.SpellClick.LEFT) a += "§bL"
                        }

                        player.sendTitle("", a.joinToString(" §7- "), 0, 10, 0)

                        if (playerSkillState[player]!!.expireTimer == 0) {
                            playerSkillState.remove(player)
                            player.sendTitle("", a.joinToString("§7CANCEL"), 0, 10, 0)
                            player.playSound(player, Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 2.0f)
                            cancel()
                        }
                    }
                }
            }.runTaskTimer(instance, 0L, 1L)
        }

        // register
        playerSkillState[player]!!.spell += click
        playerSkillState[player]!!.expireTimer = 15

        when(click) {
            ProfessionSkillState.SpellClick.RIGHT -> player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            ProfessionSkillState.SpellClick.LEFT -> player.playSound(player, Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.6f)
        }

        // cast
        if (playerSkillState[player]!!.spell.size == 3) {
            val spell = playerSkillState[player]!!.spell.drop(1)
            val profession = PlayerProfession.getProfession(player)!!

            when(profession) {
                SWORDSMAN -> Swordsman(player).callSkill(spell)
                ARCHER -> Archer(player).callSkill(spell)
                MAGE -> Mage(player).callSkill(spell)
            }

            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    playerSkillState.remove(player)
                },
                1L
            )
        }
    }

    abstract fun callSkillWithId(id: String)

    fun callSkill(spell: List<ProfessionSkillState.SpellClick>) {
        fun clickToString(spell: ProfessionSkillState.SpellClick): String {
            return when(spell) {
                ProfessionSkillState.SpellClick.LEFT -> "L"
                ProfessionSkillState.SpellClick.RIGHT -> "R"
            }
        }

        // config内のswordsman/archer/mageのいずれかを取得
        val playerProfessionId = PlayerProfession.getProfession(player)!!.toString().lowercase()

        // スペルを LR とかの形に変換
        val spellString = "${clickToString(spell[0])}${clickToString(spell[1])}"

        // config内、該当professionのうち、スペルが ↑ に一致するものを呼ぶ
        for (skillId in DataFile.playerSkill.getConfigurationSection(playerProfessionId)!!.getKeys(false)) {
            if (DataFile.playerSkill.getString("$playerProfessionId.$skillId.spell") == spellString) callSkillWithId(skillId)
        }
    }
}
