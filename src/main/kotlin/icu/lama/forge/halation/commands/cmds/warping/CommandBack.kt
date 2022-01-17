package icu.lama.forge.halation.commands.cmds.warping

import icu.lama.forge.halation.commands.CommandBase
import icu.lama.forge.halation.utils.ChatColor
import icu.lama.forge.halation.utils.EventHandler
import icu.lama.forge.halation.utils.sendMessage
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.entity.living.LivingDeathEvent
import java.util.*

object CommandBack : CommandBase {
    private val lastDeathLocation = HashMap<UUID, Location>()

    override val registeredPermissions: HashMap<String, String> = hashMapOf()
    override val baseName: String = "back"

    override fun execute(sender: ServerPlayer, args: Array<String>) {
        val location = lastDeathLocation[sender.uuid]
        if (location != null) {
            sender.teleportTo(location.level, location.x, location.y, location.z, sender.xRot, sender.yRot)
        } else {
            sender.sendMessage("${ChatColor.RED}你还没有死过，无法使用这个命令")
        }
    }

    @EventHandler fun onPlayerDeath(e: LivingDeathEvent) {
        if(e.entityLiving is ServerPlayer) {
            lastDeathLocation[e.entityLiving.uuid] = Location(e.entityLiving.x, e.entityLiving.y, e.entityLiving.z, e.entityLiving.level as ServerLevel)
        }
    }

    data class Location(val x: Double, val y: Double, val z: Double, val level: ServerLevel)
}