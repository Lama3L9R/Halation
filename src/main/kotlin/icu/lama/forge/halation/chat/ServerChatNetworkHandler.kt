package icu.lama.forge.halation.chat

import icu.lama.forge.halation.HalationForge
import icu.lama.forge.halation.utils.ChatColor
import icu.lama.forge.halation.utils.debug.HomeEntity
import icu.lama.forge.halation.utils.then
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.dimension.DimensionType

object ServerChatNetworkHandler {
    fun handle(source: ServerPlayer, msg: String): Component {
        findNotice(msg).forEach {
            //todo impl
        }
        return TextComponent("${ChatColor.GRAY}[${getWorldName(source)}${ChatColor.GRAY}] ${ChatColor.GRAY}[${ChatColor.GREEN}${PrefixManager[source.uuid]}${ChatColor.GRAY}] ").append(source.name).append(" ${ChatColor.GRAY}: ${ChatColor.RESET}${msg}")
    }

    private fun getWorldName(player: ServerPlayer): String {
        return when {
            player.getLevel().dimensionType().bedWorks() -> "${ChatColor.GREEN}主世界${ChatColor.RESET}"
            !player.getLevel().dimensionType().bedWorks() && player.getLevel().dimensionType().respawnAnchorWorks() -> "${ChatColor.RED}下届${ChatColor.RESET}"
            !player.getLevel().dimensionType().bedWorks() && !player.getLevel().dimensionType().respawnAnchorWorks() -> "${ChatColor.LIGHT_PURPLE}末地${ChatColor.RESET}"

            else -> ChatColor.AQUA + (player.getLevel().dimension()?.location()?.toString() ?: "${ChatColor.GRAY}未知") + ChatColor.RESET
        }
    }

    @HomeEntity("cn.thelama.homeent.notice.Notice#parseMessage") fun findNotice(message: String): List<ServerPlayer> {
        var index = message.indexOf('@', 0)
        val players = mutableListOf<ServerPlayer>()
        while(index != -1) {
            var spaceIndex = message.indexOf(' ', index)
            if(spaceIndex == -1) {
                spaceIndex = message.length - 1
            }
            val player = message.substring(index + 1, spaceIndex)

            HalationForge.theServer!!.playerList.getPlayerByName(player)?.let(players::add)
            index = message.indexOf('@', spaceIndex)
        }
        return players
    }
}