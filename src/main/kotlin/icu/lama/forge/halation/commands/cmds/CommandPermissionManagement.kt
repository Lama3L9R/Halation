package icu.lama.forge.halation.commands.cmds

import icu.lama.forge.halation.HalationForge
import icu.lama.forge.halation.commands.CommandBase
import icu.lama.forge.halation.secure.PermissionManagerEX
import icu.lama.forge.halation.utils.ChatColor
import icu.lama.forge.halation.utils.sendMessage
import icu.lama.forge.halation.utils.toServerPlayer
import net.minecraft.server.level.ServerPlayer
import java.util.HashMap

// TODO Test
object CommandPermissionManagement : CommandBase {
    override val registeredPermissions: HashMap<String, String> = hashMapOf(
        "pex.admin" to "Allows the user to execute all pex commands"
    )
    override val baseName: String = "pex"

    override fun execute(sender: ServerPlayer, args: Array<String>) {
        if(args.size < 2) {
            sender.sendMessage("${ChatColor.RED}Usage: /pex <group|player> <identifier> [operation] [operation-args]")
            return
        }

        val type = args[0]
        val identifier = args[1]

        if(type == "group") {
            handleGroup(sender, identifier, args.drop(2))
        } else {
            handlePlayer(sender, HalationForge.theServer!!.playerList.getPlayerByName(identifier), args.drop(2))
        }
    }

    fun handleGroup(sender: ServerPlayer, identifier: String, args: List<String>) {
        if(args.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Usage: /pex group <identifier> [operation] [operation-args]")
            return
        }

        when(args[0]) {
            "grant" -> {
                if(args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Usage: /pex group <identifier> grant <permission>")
                    return
                }

                val permission = args[1]
                PermissionManagerEX.Group.grant(identifier, permission)
                sender.sendMessage("${ChatColor.GREEN}Granted '$permission' to group '$identifier'")
            }

            "revoke" -> {
                if(args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Usage: /pex group <identifier> revoke <permission>")
                    return
                }

                val permission = args[1]
                PermissionManagerEX.Group.revoke(identifier, permission)
                sender.sendMessage("${ChatColor.GREEN}Revoked '$permission' from group '$identifier'")
            }

            "restrict" -> {
                if(args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Usage: /pex group <identifier> restrict <permission>")
                    return
                }

                val permission = args[1]
                PermissionManagerEX.Group.restrict(identifier, permission)
                sender.sendMessage("${ChatColor.GREEN}Restricted '$permission' from group '$identifier'")
            }

            "create" -> {
                PermissionManagerEX.Group.create(identifier)
                sender.sendMessage("${ChatColor.GREEN}Created group '$identifier'")
            }

            "add" -> {
                if(args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Usage: /pex group <identifier> add <player>")
                    return
                }

                val player = args[1].toServerPlayer()?.uuid

                if(player == null) {
                    sender.sendMessage("${ChatColor.GREEN}'${args[1]}' Dose not exist")
                    return
                }

                PermissionManagerEX.Group.addMember(identifier,  player)
                sender.sendMessage("${ChatColor.GREEN}Added '$player' to group '$identifier'")
            }

            "remove" -> {
                if(args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Usage: /pex group <identifier> remove <player>")
                    return
                }

                val player = args[1].toServerPlayer()?.uuid

                if(player == null) {
                    sender.sendMessage("${ChatColor.GREEN}'${args[1]}' Dose not exist")
                    return
                }

                PermissionManagerEX.Group.removeMember(identifier,  player)
                sender.sendMessage("${ChatColor.GREEN}Removed '$player' from group '$identifier'")
            }

            "delete" -> {
                PermissionManagerEX.Group.delete(identifier)
                sender.sendMessage("${ChatColor.GREEN}Deleted group '$identifier'")
            }

            else -> {
                sender.sendMessage("${ChatColor.RED}Usage: /pex group <identifier> [operation] [operation-args]")
            }
        }
    }

    private fun handlePlayer(sender: ServerPlayer, identifier: ServerPlayer?, args: List<String>) {
        if(identifier == null) {
            sender.sendMessage("${ChatColor.RED}???????????????!")
            return
        }

        if(args.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Usage: /pex player <identifier> <operation> [operation-args]")
            return
        }

        when(args[0]) {
            "grant" -> {
                if(args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Usage: /pex player <identifier> grant <permission>")
                    return
                }

                val permission = args[1]
                PermissionManagerEX.Player.grant(identifier.uuid, permission)

                identifier.sendMessage("${ChatColor.GREEN}??????????????? '${permission}' ??????!")
                sender.sendMessage("${ChatColor.GREEN}???????????? '${identifier.name}' ??? '${permission}' ??????!")
            }

            "revoke" -> {
                if(args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Usage: /pex player <identifier> revoke <permission>")
                    return
                }

                val permission = args[1]
                PermissionManagerEX.Player.revoke(identifier.uuid, permission)

                identifier.sendMessage("${ChatColor.RED}?????? '${permission}' ???????????????!")
                sender.sendMessage("${ChatColor.GREEN}???????????? '${identifier.name}' ??? '${permission}' ??????!")
            }

            "restrict" -> {
                if(args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Usage: /pex player <identifier> restrict <permission>")
                    return
                }

                val permission = args[1]
                PermissionManagerEX.Player.restrict(identifier.uuid, permission)

                identifier.sendMessage("${ChatColor.RED}?????? '${permission}' ??????????????????!")
                sender.sendMessage("${ChatColor.GREEN}???????????? '${identifier.name}' ??? '${permission}' ??????!")
            }

            else -> {
                sender.sendMessage("${ChatColor.RED}Usage: /pex player <identifier> <operation> [operation-args]")
            }
        }
    }
}