package icu.lama.forge.halation.commands.cmds

import icu.lama.forge.halation.chat.PrefixManager
import icu.lama.forge.halation.commands.CommandBase
import icu.lama.forge.halation.utils.ChatColor
import icu.lama.forge.halation.utils.sendMessage
import net.minecraft.server.level.ServerPlayer
import java.util.HashMap

object CommandPrefix : CommandBase {
    override val registeredPermissions: HashMap<String, String> = hashMapOf(
        "prefix.self" to "Allows the user to set their prefix",
        "prefix.admin" to "Allows the user to set other players' prefixes"
    )
    override val baseName: String = "prefix"

    override fun execute(sender: ServerPlayer, args: Array<String>) {
        if(args.isEmpty()) {
            sender.sendMessage("§c正确的使用方法: /prefix <prefix> | /prefix <name> <prefix>")
            return
        }

        when(args.size) {
            1 -> {
                PrefixManager[sender.uuid] = args[0]
                sender.sendMessage("${ChatColor.GREEN}已成功更新玩家 ${sender.name.contents} 的前缀")
            }

            2 -> {
                val target = sender.server.playerList.getPlayerByName(args[0])
                if(target == null) {
                    sender.sendMessage("§c无法找到玩家 ${args[0]}")
                    return
                }

                PrefixManager[target.uuid] = args[1]
                sender.sendMessage("${ChatColor.GREEN}已成功更新玩家 ${args[0]} 的前缀")
            }

            else -> {
                sender.sendMessage("§c正确的使用方法: /prefix <prefix> | /prefix <name> <prefix>")
            }
        }
    }
}