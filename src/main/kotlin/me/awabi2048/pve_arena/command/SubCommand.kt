package me.awabi2048.pve_arena.command

import me.awabi2048.pve_arena.Main.Companion.activeParty
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.menu.PartyMenu
import me.awabi2048.pve_arena.party.Party
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

class SubCommand(private val sender: Player, private val args: Array<out String>, private val option: Option) {
    fun execute() {
        fun party() {
            // パーティーに所属中かどうか
            if (activeParty.any {it.players.contains(sender)}) {
                // open menu
                val menu = PartyMenu(sender)
                menu.open()

                sender.playSound(sender, Sound.ENTITY_VILLAGER_YES, 1.0f, 2.0f)
                sender.playSound(sender, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

            } else {
                // create party
                val party = Party(sender.uniqueId.toString(), "${sender.displayName}§fのパーティー", "", 6, false, mutableSetOf(sender), sender)
                party.register()

                val menu = PartyMenu(sender)
                menu.open()

                sender.sendMessage("$prefix §b新しいパーティーを作成しました！")
                sender.playSound(sender, Sound.ENTITY_CAT_AMBIENT, 1.0f, 1.2f)
                sender.playSound(sender, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            }
        }

        when(option) {
            Option.PARTY -> party()
        }
    }

    enum class Option {
        PARTY,
    }
}