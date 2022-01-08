package icu.lama.forge.halation.chat

import icu.lama.forge.halation.utils.ChatColor
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.dimension.DimensionType

object ServerChatNetworkHandler {
    fun handle(source: ServerPlayer, msg: String): Component {
        // TODO Get prefix from PrefixManager
        // TODO Check is mute
        return TextComponent("${ChatColor.GRAY}[${getWorldName(source)}${ChatColor.GRAY}] ${ChatColor.GRAY}[${ChatColor.GREEN}Innocent${ChatColor.GRAY}] ").append(source.name).append(" ${ChatColor.GRAY}: ${ChatColor.RESET}${msg}")
    }

    private fun getWorldName(player: ServerPlayer): String {
        return when {
            player.getLevel().dimensionType().bedWorks() -> "${ChatColor.GREEN}主世界${ChatColor.RESET}"
            !player.getLevel().dimensionType().bedWorks() && player.getLevel().dimensionType().respawnAnchorWorks() -> "${ChatColor.RED}下届${ChatColor.RESET}"
            !player.getLevel().dimensionType().bedWorks() && !player.getLevel().dimensionType().respawnAnchorWorks() -> "${ChatColor.LIGHT_PURPLE}末地${ChatColor.RESET}"

            else -> ChatColor.AQUA + (player.getLevel().dimension()?.location()?.toString() ?: "${ChatColor.GRAY}未知") + ChatColor.RESET
        }
    }
}