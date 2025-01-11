package me.awabi2048.pve_arena.menu_manager.entrance

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun rotateOption(current: Int, delta: Int, option: List<Pair<String, Material>>): ItemStack {
    // 返すoptionのindexの設定: 範囲外ならいい感じに丸める
    var optionIndex = current + delta

    if (optionIndex >= option.size) {
        optionIndex = 0
    } else if (optionIndex < 0) {
        optionIndex = option.size
    }

    // lore の設定
    // 要素名を列挙
    val optionName: MutableList<String> = mutableListOf("§eクリックして変更します。", "")
    for (element in option) {
        optionName + element.first
    }

    // 該当optionに選択マーク表示; +2 は初期の案内オプション
    optionName[optionIndex + 2] = "§6▶ §l${optionName[optionIndex + 2]}"

    // loreを代入、ItemStack返す
    val optionItem = ItemStack(option[optionIndex].second, optionIndex + 1)

    val optionItemMeta = optionItem.itemMeta
    optionItemMeta.lore = optionName

    optionItem.itemMeta = optionItemMeta

    return optionItem

}
