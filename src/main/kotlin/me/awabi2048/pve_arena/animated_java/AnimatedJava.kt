package me.awabi2048.pve_arena.animated_java

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import kotlin.math.roundToInt

class AnimatedJava(val id: String) {
    fun summon(location: Location): Entity {
        val x = ((location.blockX + 0.5) * 10).roundToInt() * 0.1
        val y = location.blockY
        val z = ((location.blockZ + 0.5) * 10).roundToInt() * 0.1

        val pitch = location.pitch
        val yaw = location.yaw

        Bukkit.dispatchCommand(Bukkit.getConsoleSender()
            , "execute positioned $x $y $z rotated $pitch $yaw run function animated_java:$id/summon")
        val rigEntity = location.world.entities.find {it.location.distance(location) < 0.01 && it.scoreboardTags.contains("animated_java.core.$id")}!!
        return rigEntity
    }

    fun remove() {

    }


}