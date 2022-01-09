package icu.lama.forge.halation.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import icu.lama.forge.halation.HalationForge
import icu.lama.forge.halation.utils.ChatColor
import icu.lama.forge.halation.utils.doc
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.TextComponent

// todo need test
object CommandManualMangoDBOperation {
    object UpdateMany : Command<CommandSourceStack> {
        override fun run(context: CommandContext<CommandSourceStack>?): Int {
            if(context == null) {
                return -1
            }

            val target = context.getArgument("target", String::class.java)
            val filter = doc(context.getArgument("filter", String::class.java))
            val document = doc(context.getArgument("document", String::class.java))

            HalationForge.mongoDatabase.getCollection(target).updateMany(filter, document)

            return 0
        }
    }

    object Insert : Command<CommandSourceStack> {
        override fun run(context: CommandContext<CommandSourceStack>?): Int {
            if(context == null) {
                return -1
            }

            val target = context.getArgument("target", String::class.java)
            val document = doc(context.getArgument("document", String::class.java))

            HalationForge.mongoDatabase.getCollection(target).insertOne(document)

            return 0
        }
    }

    object Delete : Command<CommandSourceStack> {
        override fun run(context: CommandContext<CommandSourceStack>?): Int {
            if(context == null) {
                return -1
            }

            val target = context.getArgument("target", String::class.java)
            val filter = doc(context.getArgument("filter", String::class.java))

            HalationForge.mongoDatabase.getCollection(target).deleteOne(filter)

            return 0
        }
    }

    object Find : Command<CommandSourceStack> {
        override fun run(context: CommandContext<CommandSourceStack>?): Int {
            if(context == null) {
                return -1
            }

            val target = context.getArgument("target", String::class.java)
            val filter = doc(context.getArgument("filter", String::class.java))

            context.source.sendSuccess(TextComponent("${ChatColor.GREEN}Result From MongoDB ${ChatColor.GRAY}==> "), true)
            HalationForge.mongoDatabase.getCollection(target).find(filter).forEach {
                context.source.sendSuccess(TextComponent(it.toJson()), true)
            }

            return 0
        }
    }
}