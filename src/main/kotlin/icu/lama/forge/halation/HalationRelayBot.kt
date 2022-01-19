package icu.lama.forge.halation

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.asFromUserMessage
import dev.inmo.tgbotapi.utils.PreviewFeature
import io.ktor.client.engine.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object HalationRelayBot {
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


    @OptIn(PreviewFeature::class)
    suspend fun init() {
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
            }.join()
        }.join()
    }

    private fun parseTime(tStr: String): Date {
        if(tStr.indexOf('.') >= 0) {
            return _parseToTime()
        }

        if(tStr.length % 2 != 0) {
            throw IllegalArgumentException("Cannot parse!")
        }

        val ins = Instant.now()

        for (i in tStr.indices step 2) {
            ins.plusSeconds((timeMultiplier[i] ?: 0).toLong() * (tStr[i + 1].code - 48).toLong()) // todo fix this
        }

        return Date.from(ins)
    }

    private fun _parseToTime(): Date {
        TODO("impl this")
    }
}

suspend fun main() {
    HalationRelayBot.init()
}