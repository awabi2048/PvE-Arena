package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main
import me.awabi2048.pve_arena.Main.Companion.activeSession
import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.lobbyOriginLocation
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Objective
import org.codehaus.plexus.util.FileUtils

abstract class Generic(val uuid: String, val players: Set<Player>, var status: Status = Status.Standby) {

    val originLocation = Location(Bukkit.getWorld("arena_session.${uuid}"), 0.5, 0.0, 0.5)

    fun summonMobFromId(id: String, world: World, location: Location): Entity {
        val mobData = DataFile.mobDefinition.getConfigurationSection(id)!!
        val entityType = EntityType.valueOf(mobData.getString("entity_type") ?: throw IllegalStateException("Invalid entity type for $id specified. @stage_data/mob_definition.yml"))

        val mob = world.spawnEntity(location, entityType) as LivingEntity
        mob.customName = mobData.getString("display_name")
        mob.isCustomNameVisible = true

        // armor
        mob.equipment!!.helmet =
            ItemStack(Material.getMaterial(mobData.getString("equipment.helmet") ?: "AIR") ?: Material.AIR)
        mob.equipment!!.chestplate =
            ItemStack(Material.getMaterial(mobData.getString("equipment.chestplate") ?: "AIR") ?: Material.AIR)
        mob.equipment!!.leggings =
            ItemStack(Material.getMaterial(mobData.getString("equipment.leggings") ?: "AIR") ?: Material.AIR)
        mob.equipment!!.boots =
            ItemStack(Material.getMaterial(mobData.getString("equipment.boots") ?: "AIR") ?: Material.AIR)

        mob.equipment!!.helmetDropChance = -1.0f
        mob.equipment!!.chestplateDropChance = -1.0f
        mob.equipment!!.leggingsDropChance = -1.0f
        mob.equipment!!.bootsDropChance = -1.0f

        // hands
        mob.equipment!!.setItemInMainHand(
            ItemStack(
                Material.getMaterial(
                    mobData.getString("equipment.hand") ?: "AIR"
                ) ?: Material.AIR
            )
        )
        mob.equipment!!.setItemInOffHand(
            ItemStack(
                Material.getMaterial(
                    mobData.getString("equipment.offhand") ?: "AIR"
                ) ?: Material.AIR
            )
        )

        mob.equipment!!.itemInMainHandDropChance = -1.0f
        mob.equipment!!.itemInOffHandDropChance = -1.0f

        // attribute
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = mobData.getDouble("base_stats.health")
        mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.baseValue = mobData.getDouble("base_stats.strength")
        mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.baseValue = mobData.getDouble("base_stats.speed")

        val scaleModifier = (90..110).random().toDouble() / 100
        mob.getAttribute(Attribute.GENERIC_SCALE)!!.baseValue = mobData.getDouble("scale") * scaleModifier

        mob.health = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue

        // 識別タグ
        mob.scoreboardTags.add("arena.mob")

        if (mob is Zombie) mob.isBaby = false
        mob.maximumNoDamageTicks = 2

        mob.getAttribute(Attribute.GENERIC_FOLLOW_RANGE)!!.baseValue = 64.0

        return mob
    }

    fun getSessionWorld(): World? {
        return Bukkit.getWorld("arena_session.${uuid}")
    }

    fun timeTracking(order: Int) {
        object : BukkitRunnable() {
            override fun run() {
                try { // タイム加算
                    (status as Status.InGame).timeElapsed += 1
                    val timeElapsed = (status as Status.InGame).timeElapsed

                    val timeBefore = Lib.tickToClock(timeElapsed - 1)
                    val time = Lib.tickToClock(timeElapsed)

                    // スコアボード表示再設定
                    getSessionWorld()!!.players.forEach {
                        val displayScoreboard = Main.displayScoreboardMap[it]!!

                        displayScoreboard.scoreboard!!.resetScores("§fTime §7$timeBefore")
                        if (timeElapsed == 1) displayScoreboard.scoreboard!!.resetScores("§fTime §700:00.00")

                        displayScoreboard.getScore("§fTime §7$time").score = order
                    }
                } catch (e: Exception) {
                    cancel()
                    return
                }

                if (getSessionWorld()!!.players.isEmpty()) cancel()
            }
        }.runTaskTimer(instance, 0, 1)
    }

    fun stop() {
        // teleport back
        getSessionWorld()!!.players.forEach {
            it.teleport(lobbyOriginLocation)
            it.playSound(it, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 2.0f)
        }

//        val worldFile = getSessionWorld()!!.worldFolder
//        Bukkit.unloadWorld(getSessionWorld()!!, false) // ← セーブすると大遅延が発生
        getSessionWorld()!!.entities.forEach {it.remove()}
        activeSession.remove(this)
    }

    fun afkCheck() {
        val secondToTimeout = DataFile.config.getInt("misc.game.second_until_timeout").toLong()

        object : BukkitRunnable() {
            override fun run() {
                if (players.filter { it.noActionTicks >= secondToTimeout }.size == players.size) {
                    players.forEach {
                        it.sendMessage("$prefix §cセッションがタイムアウトしました。")
                        it.playSound(it, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f)
                    }
                    stop()
                    cancel()
                } else if (Lib.lookForSession(uuid) == null) {
                    cancel()
                }
            }
        }.runTaskTimer(instance, 0, secondToTimeout)
    }
    fun startWatchMobMovement() {
        object: BukkitRunnable() {
            override fun run() {
                val sessionWorld = getSessionWorld()!!
                sessionWorld.entities.filterIsInstance<Blaze>().forEach {
                    if (it.velocity.y > 0.0) it.velocity.y = 0.0
                }

                if (status !is Status.InGame) cancel()
            }
        }.runTaskTimer(instance, 0, 1)
    }

    abstract fun joinPlayer(player: Player)
    abstract fun generate()
    abstract fun start()
    abstract fun setupScoreboard(player: Player)
    abstract fun endProcession(clearTime: Int)

    sealed class Status {
        data object Standby : Status()
        data object WaitingGeneration : Status()
        data object WaitingStart : Status()
        data class InGame(
            var mobType: WaveProcessingMode.MobType,
            var difficulty: WaveProcessingMode.MobDifficulty,
            var timeElapsed: Int,
            var wave: Int,
        ) : Status()

        data object WaitingFinish : Status()
    }
}
