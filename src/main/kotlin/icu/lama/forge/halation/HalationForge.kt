package icu.lama.forge.halation

import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import net.minecraftforge.fml.loading.FMLEnvironment
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT

@Mod("halation")
object HalationForge {
    val logger: Logger = LogManager.getLogger()

    init {
        FORGE_BUS.addListener(::registerCommands)
        FORGE_BUS.addListener(::onServerInit)
        logger.info("Halation mode: ${if(FMLEnvironment.dist.isClient) "Client" else "Server"}")

        if(FMLEnvironment.dist.isClient) {

        }
    }

    private fun registerCommands(event: RegisterCommandsEvent) {

    }

    private fun onServerInit(event: FMLCommonSetupEvent) {
        LOADING_CONTEXT.registerConfig(ModConfig.Type.SERVER, HalationConfig.configSpec, "halation.toml")
    }
}