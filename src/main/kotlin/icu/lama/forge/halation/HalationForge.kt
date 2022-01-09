package icu.lama.forge.halation

import com.mojang.brigadier.arguments.StringArgumentType
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import icu.lama.forge.halation.commands.CommandManualMangoDBOperation
import net.minecraft.commands.Commands
import net.minecraft.gametest.framework.TestCommand
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.loading.FMLEnvironment
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT


@Mod("halation")
object HalationForge {
    val logger: Logger = LogManager.getLogger()
    lateinit var mongoClient: MongoClient
    lateinit var mongoDatabase: MongoDatabase

    init {
        FORGE_BUS.addListener(::registerCommands)

        logger.info("Halation mode: ${if(FMLEnvironment.dist.isClient) "Client" else "Server"}")
        if(!FMLEnvironment.dist.isClient) {
            kotlin.runCatching {
                Class.forName("com.mongodb.MongoClient")
            }.onFailure {
                if(it is ClassNotFoundException) {
                    logger.error("Where the fuck dose MongoDB goes????")
                }
                throw it
            }

            LOADING_CONTEXT.registerConfig(ModConfig.Type.SERVER, HalationConfig.configSpec, "halation.toml")
            mongoClient = MongoClient(HalationConfig.Mongodb.mongodbURL.get())
            mongoDatabase = mongoClient.getDatabase(HalationConfig.Mongodb.database.get())
        }
    }

    private fun registerCommands(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Commands.literal("db")
                .then(Commands.literal("find").requires { it.hasPermission(4) }
                    .then(Commands.argument("target", StringArgumentType.string()))
                    .then(Commands.argument("filter", StringArgumentType.string()))
                    .executes(CommandManualMangoDBOperation.Find))
                .then(Commands.literal("update").requires { it.hasPermission(4) }
                    .then(Commands.argument("target", StringArgumentType.string()))
                    .then(Commands.argument("filter", StringArgumentType.string()))
                    .then(Commands.argument("document", StringArgumentType.string()))
                    .executes(CommandManualMangoDBOperation.UpdateMany))
                .then(Commands.literal("delete").requires { it.hasPermission(4) }
                    .then(Commands.argument("target", StringArgumentType.string()))
                    .then(Commands.argument("filter", StringArgumentType.string()))
                    .executes(CommandManualMangoDBOperation.Delete))
                .then(Commands.literal("insert").requires { it.hasPermission(4) }
                    .then(Commands.argument("target", StringArgumentType.string()))
                    .then(Commands.argument("document", StringArgumentType.string()))
                    .executes(CommandManualMangoDBOperation.Insert))
        )
    }
}