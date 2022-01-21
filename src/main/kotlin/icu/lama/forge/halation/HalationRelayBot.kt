package icu.lama.forge.halation

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onContentMessage
import dev.inmo.tgbotapi.extensions.utils.asFromUserMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.parseCommandsWithParams
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.PreviewFeature
import icu.lama.forge.halation.utils.ChatColor
import icu.lama.forge.halation.utils.debug.HomeEntity
import icu.lama.forge.halation.utils.toComponent
import io.ktor.client.engine.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.*
import net.minecraft.network.chat.ChatType
import net.minecraft.server.players.UserBanListEntry
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object HalationRelayBot {
    private val chatID = ChatId(HalationConfig.TGBot.group.get())
    private val timeMultiplier: Map<Char, Int> = mapOf(
        'y' to 31536000,
        'm' to 2628000,
        'd' to 86400,
        'h' to 3600,
        'M' to 60,
        's' to 1
    )
    private val noForwardList = mutableListOf<String>()
    private val binding = ConcurrentHashMap<UUID, String>()
    private val botScope = CoroutineScope(Dispatchers.IO)
    private lateinit var bot: TelegramBot

    @OptIn(DelicateCoroutinesApi::class)
    fun launch() {
        GlobalScope.launch {
            init()
        }
    }

    @OptIn(PreviewFeature::class)
    private suspend fun init() {
        bot = telegramBot(HalationConfig.TGBot.token.get()) {
            engine {
                if(HalationConfig.TGBot.proxy.get()) {
                    this.proxy = ProxyConfig(if(HalationConfig.TGBot.isHttp.get()) { Proxy.Type.HTTP } else { Proxy.Type.SOCKS }, InetSocketAddress.createUnresolved(HalationConfig.TGBot.url.get(), HalationConfig.TGBot.port.get()))
                }
            }
        }

        botScope.launch {
            bot.buildBehaviourWithLongPolling(botScope, defaultExceptionsHandler = {
                if(it !is CancellationException) {
                    it.printStackTrace()
                    println("Relay bot running into an error")
                }
            }) {
                onCommand("online", requireOnlyCommandInMessage = false) { cxt ->
                    val pl = HalationForge.theServer!!.playerList.players.map { it?.name?.contents ?: "null" }

                    if(pl.isEmpty()) {
                        sendTextMessage(cxt.chat, "群服务器没人在线 :(")
                    } else {
                        sendTextMessage(cxt.chat, "群服务器当前在线人数: ${pl.size}\n${pl.joinToString("\n")}")
                    }
                }

                onCommand("help", requireOnlyCommandInMessage = false) { cxt ->
                    sendTextMessage(cxt.chat, "指令列表:\n" +
                            "/online - 查看群服务器在线人数\n" +
                            "/help - 查看指令列表\n" +
                            "/bind <player> <bind secret> - 绑定账号\n" +
                            "/kick <player> - 踢出玩家\n" +
                            "/ban <player> <time> <reason> - 封禁玩家\n" +
                            "  ex: /ban 1y2m3d4h5M6s Use jndi ldap exploit\n" +
                            "  ex: /ban 2023.1.19/8:00:00 Use jndi ldap exploit\n" +
                            "/toggle - 切换relay转发模式\n" +
                            "/feed - 切换relay转发模式\n" +
                            "/unban <player> - 解封玩家\n")
                }

                onCommand("toggle") {
                    val msg = it.asFromUserMessage()
                    if(msg != null) {
                        if(msg.from.username?.username in noForwardList) {
                            noForwardList -= msg.from.username?.username ?: ""
                            reply(it, "您的转发模式已切换到：**<u>转发<u/>**")
                        } else {
                            noForwardList += msg.from.username?.username ?: ""
                            reply(it, "不转发")
                        }
                    }
                }

                onCommand("ban", requireOnlyCommandInMessage = false) {
                    val args = it.parseCommandsWithParams()["ban"]
                    if(args != null && args.size >= 2) {
                        val target = HalationForge.theServer!!.playerList.getPlayerByName(args[0])
                        val t = parseTime(args[1])
                        val reason = args.drop(2).joinToString(" ")

                        HalationForge.theServer.playerList.bans.add(UserBanListEntry(target.gameProfile, ))  // todo what the fuck did params means???
                    }
                }

                @HomeEntity("cn.thelama.homeent.relay.RelayBotV2") onContentMessage {
                    when(val content = it.content) {
                        is TextContent -> {
                            if(HalationConfig.TGBot.group.get() == it.chat.id.chatId) {
                                val usr = it.asFromUserMessage()?.user
                                if((usr?.username ?: "") in noForwardList) {
                                    return@onContentMessage
                                }

                                val name = if(usr?.firstName == null && usr?.lastName == null) {
                                    usr?.username?.username
                                } else {
                                    "${usr.firstName} ${usr.lastName}"
                                }
                                // todo Do this need to sync up?
                                HalationForge.theServer!!.playerList.broadcastMessage("${ChatColor.AQUA}[${ChatColor.GREEN}RELAY${ChatColor.AQUA}] ${ChatColor.YELLOW}$name${ChatColor.RESET}: ${content.text}".toComponent(), ChatType.CHAT, null)
                            } else {
                                println("Ignored non-target chat message from: ${it.chat.id.chatId}")
                            }
                        }

                        else -> {
                            println("Ignored ${it.content::class.simpleName} type of chat message from: ${it.chat.id.chatId}")
                        }
                    }
                }
            }.join()
        }.join()
    }

    fun callOnPlayerChat(uuid: UUID, msg: String) {
        botScope.launch {
            if (binding[uuid] !in noForwardList) {
                bot.sendTextMessage(chatID, "${binding[uuid]}: $msg")
            }
        }
    }

    fun parseTime(tStr: String): Date {
        if(tStr.indexOf('.') != -1) {
            return parseToTime(tStr)
        }

        if(tStr.length % 2 != 0) {
            throw IllegalArgumentException("Cannot parse time string to Date object! tString: $tStr")
        }

        var ins = Instant.now()

        for (i in tStr.indices step 2) {
            ins = ins.plusSeconds((timeMultiplier[tStr[i + 1]] ?: 0).toLong() * (tStr[i].code - 48).toLong()) // todo fix this
        }

        println("d: $ins")

        return Date.from(ins)
    }
    private fun parseToTime(tStr: String): Date {
        println("_Parse called")
        val pts = tStr.split("/")
        val cal = GregorianCalendar()
        val sysCal = Calendar.getInstance()
        if(pts.size == 1) {
            val raw = pts[0]
            if(raw.contains(":")) {
                val t = raw.split(":").map { it.toInt() }
                if(t.size < 3) {
                    throw IllegalArgumentException("Cannot parse time string to Date object! tString: $tStr")
                }
                cal.set(sysCal.get(Calendar.YEAR), sysCal.get(Calendar.MONTH), sysCal.get(Calendar.DAY_OF_MONTH), t[0], t[1], t[2])
            }
        } else if(pts.isNotEmpty()) {
            val tRaw = pts[1].split(":").map { it.toInt() }
            val dRaw = pts[0].split(".").map { it.toInt() }

            if(tRaw.size < 3 || dRaw.size < 3) {
                throw IllegalArgumentException("Cannot parse time string to Date object! tRaw < 3 or dRaw < 3 | tString = $tStr")
            }

            cal.set(dRaw[0], dRaw[1] - 1, dRaw[2], tRaw[0], tRaw[1], tRaw[2])
        } else {
            throw IllegalArgumentException("Cannot parse time string to Date object! tString: $tStr")
        }

        return cal.time
    }
}