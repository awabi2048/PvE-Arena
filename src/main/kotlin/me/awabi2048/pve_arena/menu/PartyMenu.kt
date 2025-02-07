package me.awabi2048.pve_arena.menu

import me.awabi2048.pve_arena.Main.Companion.activeParty
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.party.ChatChannel.*
import me.awabi2048.pve_arena.party.Party.Companion.playerChatState
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class PartyMenu(player: Player) : MenuManager(player, MenuType.Party) {
    override fun open() {
        // get data
        val party = activeParty.find { it.players.contains(player) }!!

        val recruitingState = when (party.isRecruiting) {
            true -> "§a募集中"
            false -> "§c非公開"
        }
        val chatState = when(playerChatState[player]) {
            PARTY -> "§bパーティーチャット"
            else -> "§aノーマルチャット"
        }

        val menu = Bukkit.createInventory(null, 45, "§8§nParty")

        //
        for (row in 0..8) {
            menu.setItem(row, black)
            menu.setItem(row + 9, gray)
            menu.setItem(row + 18, gray)
            menu.setItem(row + 27, gray)
            menu.setItem(row + 36, black)
        }

        // disband
        val disbandIcon = ItemStack(Material.REDSTONE)
        val disbandIconMeta = disbandIcon.itemMeta
        disbandIconMeta.setItemName("§cパーティーを解散する")
        disbandIconMeta.lore = when (party.leader == player) {
            true -> listOf(
                "§7クリックしてパーティーを§c解散§7します。",
            )

            false -> listOf(
                "§7クリックしてパーティーを§c解散§7します。"
            )
        }
        disbandIcon.itemMeta = disbandIconMeta

        // recruit
        val toggleRecruitIcon = when (party.isRecruiting) {
            true -> ItemStack(Material.BOOKSHELF)
            false -> ItemStack(Material.CHISELED_BOOKSHELF)
        }
        val toggleRecruitIconMeta = toggleRecruitIcon.itemMeta
        toggleRecruitIconMeta.setItemName("§6パーティーメンバーを募集する")
        toggleRecruitIconMeta.lore = listOf(
            "§7クリックしてパーティーメンバーの募集を開始します。",
            "§7受信設定をしているプレイヤーに通知を送信します。",
            "",
            "§7« §b現在の設定 §7»",
            "§7- $recruitingState",
        )
        toggleRecruitIcon.itemMeta = toggleRecruitIconMeta

        // change name/description
        val settingIcon = ItemStack(Material.NAME_TAG)
        val settingIconMeta = settingIcon.itemMeta
        settingIconMeta.setItemName("§aパーティー情報の変更")
        settingIconMeta.lore = listOf(
            "§7パーティー名とパーティー説明の変更を行います。",
            "§7- §e左クリック§7: §f§nパーティー名§7を設定",
            "§7- §e右クリック§7: §f§nパーティー説明§7を設定",
            "",
            "§7« §b現在の設定 §7»",
            "§7- §fパーティー名§7: §7「${party.name}§7」",
            "§7- §fパーティー説明§7: §7${party.description}§7",
        )
        settingIcon.itemMeta = settingIconMeta

        // chat toggle
        val toggleChatIcon = when (playerChatState[player]) {
            PARTY -> ItemStack(Material.LIME_DYE)
            else -> ItemStack(Material.GRAY_DYE)
        }
        val toggleChatIconMeta = toggleChatIcon.itemMeta
        toggleChatIconMeta.setItemName("§6チャットを切り替える")
        toggleChatIconMeta.lore = listOf(
            "§7クリックして§fチャットの送信先§7を切り替えます。",
            "",
            "§7« §b現在の設定 §7»",
            "§7- §f送信先§7: $chatState",
        )
        toggleChatIcon.itemMeta = toggleChatIconMeta

        // party info
        val partyInfoIcon = ItemStack(Material.PLAYER_HEAD)
        val partyInfoIconMeta = partyInfoIcon.itemMeta
        partyInfoIcon.amount = party.players.size
        (partyInfoIconMeta as SkullMeta).owningPlayer = party.leader
        partyInfoIconMeta.setItemName("§e${party.name}")
        partyInfoIconMeta.lore = listOf(
            "§7${party.description}",
            "",
            "§7« §b現在のメンバー §7»",
            "§7- §e${party.leader.displayName} §f(§bリーダー§f)"
        )

        for (member in party.players - party.leader) {
            partyInfoIconMeta.lore!! += "§7- §f${member.displayName()}"
        }

        partyInfoIconMeta.lore!! += when(player == party.leader) {
            true -> "§fあなたは§bリーダー§fです。"
            false -> "§fあなたは§bリーダー§fではありません。"
        }

        partyInfoIcon.itemMeta = partyInfoIconMeta

        // invite
        val inviteIcon = ItemStack(Material.LEATHER_HELMET)
        val inviteIconMeta = inviteIcon.itemMeta
        inviteIconMeta.setItemName("§bプレイヤーを招待")
        inviteIconMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        inviteIconMeta.lore = when (party.players.size < DataFile.config.getInt("misc.game.player_max")){
            true -> listOf(
                "§7クリックしてほかのプレイヤーを§f招待§7します。",
            )

            false -> listOf(
                "§c§nパーティーはいっぱいです。",
            )
        }

        inviteIconMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)

        inviteIcon.itemMeta = inviteIconMeta

        // place icon
        menu.setItem(20, settingIcon)
        menu.setItem(22, toggleRecruitIcon)
        menu.setItem(24, disbandIcon)
        menu.setItem(38, toggleChatIcon)
        menu.setItem(40, partyInfoIcon)
        menu.setItem(42, inviteIcon)

        player.openInventory(menu)

    }
}
