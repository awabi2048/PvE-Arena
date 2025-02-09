package me.awabi2048.pve_arena.misc

import me.awabi2048.pve_arena.Main.Companion.activeSession
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.game.Generic
import me.awabi2048.pve_arena.game.WaveProcessingMode
import me.awabi2048.pve_arena.profession.ArrowType
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object Lib {
    fun getHiddenItem(material: Material): ItemStack {
        val item = ItemStack(material, 1)
        val itemMeta = item.itemMeta
        itemMeta.isHideTooltip = true

        item.itemMeta = itemMeta
        return item
    }

    fun tickToClock(tick: Int): String {
        val decimal = ((tick % 20) * 5).toString().padStart(2, '0')
        val second = ((tick / 20) % 60).toString().padStart(2, '0')
        val minute = ((tick / 20 / 60) % 60).toString().padStart(2, '0')

        return "$minute:$second.$decimal"
    }

    fun lookForSession(uuid: String): Generic? {
        return if (activeSession.any { it.uuid == uuid }) {
            activeSession.filter { it.uuid == uuid }.toList()[0]
        } else null
    }

    fun getBar(length: Int, base: String): String {
        var bar = base

        var i = 0
        while (i < length) {
            bar = "$bar▬"
            i++
        }

        return bar
    }

    fun getMobTypeSection(mobType: WaveProcessingMode.MobType): ConfigurationSection? {
        val path = mobType.toString().substringAfter("MobType.").lowercase()
        return DataFile.mobType.getConfigurationSection(path)
    }

    fun getMobDifficultySection(mobDifficulty: WaveProcessingMode.MobDifficulty): ConfigurationSection? {
        val path = mobDifficulty.toString().substringAfter("MobDifficulty.").lowercase()
        return DataFile.mobDifficulty.getConfigurationSection(path)
    }

    fun getPlayerHead(player: Player): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        val itemMeta = item.itemMeta
        if (itemMeta is SkullMeta) {
            itemMeta.owningPlayer = player
        }
        item.itemMeta = itemMeta
        return item
    }

    fun getProgressionBar(current: Int, max: Int, length: Int): String {
        val filled = (current.toDouble() / max).toInt() * length
        val unfilled = length - filled

        var bar = "§a"
        bar += "|".repeat(filled)
        bar += "§7"
        bar += "|".repeat(unfilled)

        return bar
    }

    /**
     * プレイヤーの体力を回復します。
     */
    fun healPlayer(player: Player, amount: Double) {
        val healedHealth = player.health + amount
        val playerMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        player.health = if (healedHealth > playerMaxHealth) playerMaxHealth else healedHealth
    }

    /**
     * 音・再生半径・ピッチを指定して周囲のプレイヤーに音を再生します。
     */
    fun playGlobalSound(player: Player, sound: org.bukkit.Sound, radius: Double, pitch: Float) {
        player.getNearbyEntities(radius, radius, radius).filter { it != player }.forEach {
            player.playSound(it, sound, 1.0f, pitch)
        }

        player.playSound(player, sound, 1.0f, pitch)
    }

    /**
     * 発射された矢に必要なデータを適用します。
     */
    fun setArrowAttribute(arrowEntity: AbstractArrow, bowItem: ItemStack, arrowType: ArrowType) {
        // tag
        arrowEntity.addScoreboardTag("$arrowType")

        // damage
        val baseDamage = when (arrowType) {
            ArrowType.FLINT_TIPPED -> 2.0
            ArrowType.IRON_TIPPED -> 4.0
            ArrowType.DIAMOND_TIPPED -> 7.0
            else -> 2.0
        }

        val powerEnchantLevel = bowItem.getEnchantmentLevel(Enchantment.POWER)
        val enchantmentModifier = (powerEnchantLevel + 1) * 0.25 + 1.0

        val arrowDamage = baseDamage * enchantmentModifier
        arrowEntity.damage = arrowDamage
    }

    /**
     * World から アリーナセッションを取得します。
     */
    fun getUUID(world: World): Generic? {
        if (!world.name.startsWith("arena_session.")) return null
        val uuid = world.name.substringAfter("arena_session.")
        return lookForSession(uuid)
    }

    /**
     * key に参照する項、value に重み付けを入れたmapを入力することで、weightに基づくランダムピックを行います。
     */
    fun simulateWeight(map: Map<Any, Int>): Any? {
        val weightSum = map.values.sum()
        val pickupSeed = (1..weightSum).random()
        var weightPreliminary = 1

        for (index in map.keys) {
            if (pickupSeed in weightPreliminary..<weightPreliminary + map[index]!!) {
                return index

            } else {
                weightPreliminary += map[index]!!
            }
        }

        return null
    }

    /**
     * StringからIntRangeへ変換しまし。
     */
    fun intRangeOf(rangeString: String): IntRange? {
        rangeString.contains("..")
        val minValue = rangeString.substringBefore("..").toIntOrNull()?: return null
        val maxValue = rangeString.substringAfter("..").toIntOrNull()?: return null
        if (minValue > maxValue) return null

        return minValue..maxValue
    }

    class SidebarManager(player: Player) {
        fun unregister(scoreboard: String) {

        }

        fun clear(value: String) {

        }

        fun set(value: String, rank: Int) {

        }

        fun setup(title: String) {
//            val scoreboard = Bukkit.getScoreboardManager().newScoreboard.registerNewObjective(
//                "arena_scoreboard_display.${player.uniqueId}",
//                Criteria.DUMMY,
//                "§7« §c§lArena §7»"
//            )
        }

        fun get() {

        }
    }
}
