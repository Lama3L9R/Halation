package icu.lama.forge.halation.commands.warping

import icu.lama.forge.halation.HalationForge
import icu.lama.forge.halation.commands.CommandBase
import icu.lama.forge.halation.utils.doc
import icu.lama.forge.halation.utils.sendMessage
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import java.util.*
import kotlin.concurrent.thread

object CommandHome : CommandBase {
    private val homes = HashMap<UUID, Landmark>()
    private val collection = HalationForge.mongoDatabase.getCollection("Homes")

    override val registeredPermissions: HashMap<String, String> = hashMapOf()
    override val baseName: String = "home"

    init {
        collection.find().forEach {
            homes[UUID.fromString(it["uuid"] as String)] = Landmark(it["x"] as Double, it["y"] as Double, it["z"] as Double, HalationForge.getLevel(it["dimension"] as String) ?: HalationForge.theServer!!.overworld())
        }
    }

    override fun execute(sender: ServerPlayer, args: Array<String>) {
        if(args.isNotEmpty() && args[0] == "set") {
            val new = Landmark(sender.x, sender.y, sender.z, sender.level as ServerLevel)
            homes[sender.uuid] = new
            thread {
                collection.updateOne(doc("""
                    {
                        "uuid": "${sender.uuid}"
                    }
                """.trimIndent()), doc("""
                    {
                        "x": ${new.x},
                        "y": ${new.y},
                        "z": ${new.z},
                        "dimension": "${new.dim.dimension().registryName}"
                    }
                """.trimIndent()))
            }
        } else {
            val home = homes[sender.uuid]
            if(home != null) {
                sender.teleportTo(home.dim, home.x, home.y, home.z, sender.xRot, sender.yRot)
            } else {
                sender.sendMessage("§c你没有家! (Lama注：真的不是在骂人，用 /home set 来设置当前位置为家)")
            }
        }
    }

    data class Landmark(val x: Double, val y: Double, val z: Double, val dim: ServerLevel)
}