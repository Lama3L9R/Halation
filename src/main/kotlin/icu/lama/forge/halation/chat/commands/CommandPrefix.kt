package icu.lama.forge.halation.chat.commands

import icu.lama.forge.halation.commands.CommandBase
import icu.lama.forge.halation.utils.sendMessage
import net.minecraft.server.level.ServerPlayer

object CommandPrefix : CommandBase {
    override fun execute(sender: ServerPlayer, args: Array<String>) {
        if(args.isEmpty()) {
            sender.sendMessage("§c正确的使用方法: /prefix <prefix> | /prefix <name> <prefix>")
            return
        }
    }
}