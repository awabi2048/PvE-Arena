package me.awabi2048.pve_arena.party

import me.awabi2048.pve_arena.Main.Companion.activeParty
import me.awabi2048.pve_arena.Main.Companion.prefix
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

data class Party(val uuid: String = UUID.randomUUID().toString(), var name: String = "", var description: String = "", val size: Int = 6, var isRecruiting: Boolean = false, val players: MutableSet<Player>, val leader: Player) {
    fun register() {
        activeParty += this
    }

    fun changeName(newName: String) {
        this.name = newName
        this.sendPartyMessage(null, "§7パーティーの名前が §a${newName}§7 に変更されました。")
    }

    fun changeDescription(newDescription: String) {
        this.description = newDescription
        this.sendPartyMessage(null, "§7パーティーの説明が変更されました。")
    }

    fun joinPlayer(player: Player) {
        this.players += player
        this.players.forEach {
            it.sendMessage("$prefix §7[§bParty§7] §6${player.displayName} §7さんがパーティーに参加しました！")
            it.playSound(it, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f)
            it.playSound(it, Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f)
        }
    }

    fun sendPartyMessage(sender: Player?, message: String) {
        this.players.forEach {
            it.playSound(it, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f)
            if (sender != null){
                it.sendMessage("$prefix §7[§bParty§7] §6${sender.displayName}§7: $message")
            } else {
                it.sendMessage("$prefix §7[§bParty§7] §2System§7: $message")
            }
        }
    }


    fun toggleRecruiting() {
        this.isRecruiting = when(this.isRecruiting) {
            true -> false
            false -> true
        }
    }

    companion object {
        var playerChatState: MutableMap<Player, ChatChannel> = mutableMapOf()
        var playerChatListening: MutableMap<Player, String> = mutableMapOf()
    }
}
