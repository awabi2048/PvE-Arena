package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.config.DataFile
import me.awabi2048.pve_arena.item.AccessoryItem
import me.awabi2048.pve_arena.item.ItemManager
import me.awabi2048.pve_arena.misc.Lib
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerMoveEvent
import kotlin.math.roundToInt

object GameEventListener: Listener {
    // ダンジョン
    @EventHandler // プレイヤーが部屋変更
    fun onPlayerRoomChanged(event: PlayerMoveEvent) {
        if (event.player.world.name.startsWith("arena_session.")) {
            val session = Lib.getUUID(event.player.world)!!
            if (session is ArenaDungeon && session.getSessionWorld()!!.entities.none{it.scoreboardTags.contains("arena.mob")}) {
                for (entranceEntity in event.player.world.entities.filter {it.scoreboardTags.contains("arena.game.dungeon_exit") && !it.scoreboardTags.contains("arena.game.dungeon_entrance")}) {
                    if (event.player.world.players.all { entranceEntity.boundingBox.contains(it.location.toVector()) } ) {
                        session.playerChangeRoom(entranceEntity.scoreboardTags.contains("arena.game.dungeon_floor_changer"))
                        break
                    }
                }
            }
        }
    }

    @EventHandler // モブのドロップ・経験値の量を修正
    fun dropModify(event: EntityDeathEvent) {
        if (!event.entity.location.world.name.startsWith("arena_session") || event.entity !is Monster) return
        val uuid = event.entity.location.world.name.substringAfter("arena_session.")

        if (Lib.lookForSession(uuid) is WaveProcessingMode) {
            val difficulty = (Lib.lookForSession(uuid)?.status as Generic.Status.InGame).difficulty
            val difficultyBonus = DataFile.mobDifficulty.getDouble("${difficulty.toString().substringAfter("MobDifficulty.").lowercase()}.reward_multiplier") * (50..100).random() / 100

            // exp
            event.droppedExp = (event.droppedExp * difficultyBonus * 0.5).roundToInt()
//            println("${event.droppedExp}")

            // drop
            val majorMaterial = listOf(
                Material.ROTTEN_FLESH,
                Material.BONE,
                Material.BLAZE_ROD,
                Material.STRING,
                Material.SPIDER_EYE,
                Material.PRISMARINE_SHARD,
                Material.PRISMARINE_CRYSTALS,
                Material.ENDER_PEARL,
            )

            event.drops.filter {it.type in majorMaterial}.forEach {
                it.amount = (it.amount * difficultyBonus).roundToInt()
//                println("dropped ${it.type} for ${it.amount}")
            }
        }
    }

    @EventHandler // プレイヤーが与えたダメージを修正
    fun onPlayerDealDamage(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        if (!event.entity.location.world.name.startsWith("arena_session")) return

        val player = event.damager as Player
        if (AccessoryItem.searchPlayerInventory(player, ItemManager.ArenaItem.HUNTER_ACCESSORY) > 0) {
            event.damage *= 1.3
            Lib.healPlayer(player, event.damage * 0.1)
        }
    }

    @EventHandler // プレイヤーの受けたダメージを修正
    fun onPlayerTakeDamage(event: EntityDamageByEntityEvent) {
        if (event.entity.world.name.startsWith("arena_session.")) {
            if (event.entity !is Player) return

            if (event.damager is Guardian) {
                event.damage = (event.damager as Guardian).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value
            }
        }
    }

    // モブの挙動系
    @EventHandler // スライムの分裂を防ぐやつ
    fun preventSlimeSplit(event: SlimeSplitEvent) {
        if (!event.entity.location.world.name.startsWith("arena_session")) return
        event.isCancelled = true
    }

    @EventHandler // 敵が打った飛び道具のダメージを乗せる
    fun setHitProjectileDamage(event: ProjectileHitEvent) {
        if (event.hitEntity is Player && event.entity.world.name.startsWith("arena_session.")) {
            if (event.entity.shooter is Monster) {
                (event.hitEntity as Player).damage((event.entity.shooter as LivingEntity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value,
                    event.entity.shooter as Monster
                )
            }
        }
    }

    @EventHandler // モブの同士討ちを防ぐ
    fun onMobFriendlyFire(event: EntityDamageByEntityEvent) {
        if (!event.entity.location.world.name.startsWith("arena_session")) return
        if (event.entity !is Player && event.damager !is Player) {
            // 飛び道具 → プレイヤーからの発射でなければキャンセル
            if (event.damager is Projectile) {
                if ((event.damager as Projectile).shooter !is Player) event.isCancelled = true
            } else { // それ以外
                event.isCancelled = true
            }
        }
    }

    @EventHandler // モブのターゲットを変更
    fun regulateMobTarget(event: EntityTargetLivingEntityEvent) {
        if (
            event.entity.world.name.startsWith("arena_session.")
            && event.target !is Player
            && event.entity !is Guardian
            && event.entity.world.players.isNotEmpty()
            ) {
            event.isCancelled = true
        }
    }
}
