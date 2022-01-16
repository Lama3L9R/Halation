package icu.lama.forge.halation.utils

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.common.Mod


typealias TOMLEntry<T> = ForgeConfigSpec.ConfigValue<T>
typealias TOMLBuilder = ForgeConfigSpec.Builder
typealias TOML = ForgeConfigSpec
typealias SubCommand = Command<CommandSourceStack>
typealias CommandContext = CommandContext<CommandSourceStack>

typealias static = JvmStatic
typealias EventListener = Mod.EventBusSubscriber