package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.Main.Companion.instance
import me.awabi2048.pve_arena.Main.Companion.prefix
import me.awabi2048.pve_arena.config.DataFile
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.pow

interface WaveProcessingMode {
    fun waveProcession()

    fun finishWave(world: World, wave: Int, lastWave: Int, clearTime: Int) {
        world.players.forEach {
            if (wave != lastWave) {
                it.sendMessage("$prefix §6Wave $wave §7が終了しました！§e10秒後§7に次のウェーブが開始します。")
                it.playSound(it, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        startCountdown(9, world)
                    },
                    20L
                )

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        waveProcession()
                    },
                    200L
                )

            } else {
                it.sendMessage("$prefix §6§lLast Wave§7 が終了しました！§e10秒後§7にロビーに戻ります。")
                it.playSound(it, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
                it.playSound(it, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.1f)

                // reward
                rewardDistribute()

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        endProcession(clearTime)
                    },
                    200L
                )
            }
        }

        // remove projectiles
        world.entities.filterIsInstance<Projectile>().forEach {
            it.remove()
        }
    }

    fun rewardDistribute()

    fun endProcession(clearTime: Int)

    fun startSpawnSession()

    fun randomSpawn(world: World, wave: Int, mobType: MobType, difficulty: MobDifficulty) {
        fun mobStatsCalc(id: String, data: String): Double {

            val difficultySection = DataFile.mobDifficulty.getConfigurationSection(mobDifficultyToString(difficulty))!!

            val mobData = DataFile.mobDefinition.getConfigurationSection(id)!!
            val baseValue = mobData.getDouble("base_stats.$data")

            val difficultyModifier =
                difficultySection.getDouble("mob_multiplier", 1.0)
            val waveModifier =
                1.0 + wave * DataFile.config.getDouble("mob_stats.per_wave", 0.1)
            val playerCountModifier =
                1.0 + (world.players.size - 1) * DataFile.config.getDouble("mob_stats.per_player_count", 0.1)

            val totalModifier = difficultyModifier * waveModifier * playerCountModifier

            val modifiedValue = when (data) {
                "speed" -> baseValue * totalModifier.pow(DataFile.config.getDouble("mob_stats.speed_modifier_pow", 0.1))
                else -> baseValue * totalModifier
            }

            return modifiedValue
        }

        val mobTypeSection = DataFile.mobType.getConfigurationSection(mobTypeToString(mobType))!!

        val spawnLocation =
            listOf(
                Location(world, 18.5, 0.25, 0.5),
                Location(world, 0.5, 0.25, 18.5),
                Location(world, -18.5, 0.25, 0.5),
                Location(world, 0.5, 0.25, -18.5)
            ).random()

        // select
        val availableMobSection = mobTypeSection.getConfigurationSection("mobs")!!
        val availableMobSet = availableMobSection.getKeys(false)

        val spawnCandidate: MutableList<String> = mutableListOf()
        for (key in availableMobSet) {

            // wave check
            val waveRangeString = availableMobSection.getString("$key.wave")!!
            val waveRangeMin = waveRangeString.substringBefore("..").toInt()
            val waveRangeMax = waveRangeString.substringAfter("..").toInt()
            val waveRange = waveRangeMin..waveRangeMax

            // difficulty check

            if (wave in waveRange) spawnCandidate += key
        }

        var weightSum = 0

        for (key in spawnCandidate) {
            weightSum += availableMobSection.getInt("$key.weight")
        }

        val seed = (1..weightSum).random()

        var weightSumPreliminary = 0
        var spawnMobId = "none"

        for (key in spawnCandidate) {
            if (seed in weightSumPreliminary + 1..(weightSumPreliminary + availableMobSection.getInt("$key.weight"))) {
                spawnMobId = key
                break
            }
            weightSumPreliminary += availableMobSection.getInt("$key.weight")
        }

        // spawn
//        println("CANDIDATES: $spawnCandidate, SEED:$seed ,MOB ID:$spawnMobId")

        val mobData = DataFile.mobDefinition.getConfigurationSection(spawnMobId)!!
        val entityType = EntityType.valueOf(mobData.getString("entity_type") ?: "".uppercase())

        val mob = world.spawnEntity(spawnLocation, entityType) as LivingEntity
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
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = mobStatsCalc(spawnMobId, "health")
        mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.baseValue = mobStatsCalc(spawnMobId, "strength")
        mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.baseValue = mobStatsCalc(spawnMobId, "speed")

        val scaleModifier = (90..110).random().toDouble() / 100
        mob.getAttribute(Attribute.GENERIC_SCALE)!!.baseValue = mobData.getDouble("scale") * scaleModifier

        mob.health = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue

        // 識別タグ
        mob.scoreboardTags.add("arena.mob")

        if (mob is Zombie) mob.isBaby = false
        mob.maximumNoDamageTicks = 2

        mob.getAttribute(Attribute.GENERIC_FOLLOW_RANGE)!!.baseValue = 64.0
        (mob as Monster).target = world.players.random()

        world.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, spawnLocation, 10, 0.1, 0.1, 0.1, 0.1)

    }

    fun mobDifficultyToString(difficulty: MobDifficulty): String {
        return difficulty.toString().substringAfter("MobDifficulty.").lowercase()
    }

    fun mobTypeToString(mobType: MobType): String {
        return mobType.toString().substringAfter("MobType.").lowercase()
    }

    fun startCountdown(time: Int, world: World) {
        var timeRemaining = time

        object: BukkitRunnable() {
            override fun run() {
                world.players.forEach {
                    if (timeRemaining in 1..5) {
                        it.playSound(it, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
                        it.sendTitle("", "§7- §e$timeRemaining §7-", 5, 20, 5)
                    }

                    if (timeRemaining in listOf(5, 10, 15) || timeRemaining % 15 == 0) {
                        it.sendMessage("$prefix §7開始まで残り§e§n${timeRemaining}秒§7です。")
                        it.sendTitle("", "§7- §e$timeRemaining §7-", 5, 20, 5)
                        it.playSound(it, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
                    }
                }

                timeRemaining -= 1
                if (timeRemaining == 0) cancel()
            }
        }.runTaskTimer(instance, 0, 20)
    }

    enum class MobType {
        ZOMBIE,
        SKELETON,
        SPIDER,
        BLAZE,
        GUARDIAN,
        ENDERMAN;
    }

    enum class MobDifficulty {
        EASY,
        NORMAL,
        HARD,
        EXPERT,
        NIGHTMARE;
    }
}
