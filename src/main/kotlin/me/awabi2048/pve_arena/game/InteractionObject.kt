package me.awabi2048.pve_arena.game

import me.awabi2048.pve_arena.animated_java.AnimatedJava
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import javax.annotation.Nullable

class InteractionObject(objectType: ObjectType, @Nullable val uuid: String? = null, animatedJava: AnimatedJava) {
    fun place(location: Location): Entity {
        val world = location.world ?: throw IllegalArgumentException("Location must have a valid world")
        val entity = world.spawnEntity(location, EntityType.INTERACTION)
        entity.customName = uuid
        entity.isCustomNameVisible = false
        if (entity is org.bukkit.entity.Interaction) {
            entity.isInvisible = true
            entity.setGravity(false)
        }
        return entity
    }

    enum class ObjectType {
        LOOT_CHEST_NORMAL,
        LOOT_CHEST_RARE,
        LOOT_CHEST_EPIC,
    }
}
