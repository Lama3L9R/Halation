package icu.lama.forge.halation.utils

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.common.Mod


typealias TOMLEntry<T> = ForgeConfigSpec.ConfigValue<T>
typealias TOMLBuilder = ForgeConfigSpec.Builder
typealias TOML = ForgeConfigSpec

typealias static = JvmStatic
typealias EventListener = Mod.EventBusSubscriber