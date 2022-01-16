package icu.lama.forge.halation.commands

import icu.lama.forge.halation.HalationForge
import net.minecraft.server.level.ServerPlayer

object HalationCommandRegistry {
    private val commands = mutableMapOf<String, CommandBase>()

    fun register(command: CommandBase) {
        commands[command.baseName] = command
    }

    fun execute(raw: String, sender: ServerPlayer): Boolean {
        val split = raw.substring(1).split(" ")
        val halationCommand = commands[split[0]]
        return if(halationCommand == null) {
            false
        } else {
            try {
                halationCommand.execute(sender, split.drop(1).toTypedArray())
            } catch (e: Exception) {
                e.printStackTrace()
                HalationForge.logger.error("Error while executing halation command: $raw")
            }
            true
        }
    }
}