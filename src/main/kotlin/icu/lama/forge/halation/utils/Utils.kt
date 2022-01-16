package icu.lama.forge.halation.utils

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket
import net.minecraft.server.level.ServerPlayer
import org.apache.logging.log4j.Logger
import org.bson.BsonDocument
import org.bson.Document
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.stream.Collectors

fun void(): UUID = UUID(0, 0)

fun bson(bson: String): BsonDocument {
    return BsonDocument.parse(bson)
}

fun doc(bson: String): Document {
    return Document.parse(bson)
}


fun <CallList, CompanionList, Result> List<CallList>.with(list: List<CompanionList>, callback: (CallList?, CompanionList?) -> Result): List<Result> {
    val result = mutableListOf<Result>()
    for(i in 0 until this.size.coerceAtLeast(list.size)) {
        result += callback(this.getOrNull(i), list.getOrNull(i))
    }
    return result
}

/**
 * @param id Should be generated randomly
 */
fun Logger.potential(module: String, id: String, msg: String) {
    this.warn("!! Potential Bug Report !! Source = '${module}' UniqueID = $id --> $msg")
}

fun <T, R> T.then(func: T.() -> R) = func()


fun findAllClassesUsingClassLoader(packageName: String): Set<Class<*>?>? {
    val stream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replace("[.]".toRegex(), "/"))
    val reader = BufferedReader(InputStreamReader(stream))
    return reader.lines()
        .filter { it.endsWith(".class") }
        .map { getClass(it, packageName) }
        .collect(Collectors.toSet())
}

private fun getClass(className: String, packageName: String): Class<*>? {
    try {
        return Class.forName(
            packageName + "."
                    + className.substring(0, className.lastIndexOf('.'))
        )
    } catch (e: ClassNotFoundException) { }
    return null
}

fun String.toComponent(): Component {
    return TextComponent(this)
}

fun ServerPlayer.sendMessage(msg: String) {
    this.sendMessage(msg.toComponent(), void())
}

fun ServerPlayer.sendTitle(title: String = "", subTitle: String = "", actionBar: String = "", fadeIn: Int = 2, stay: Int = 5, fadeOut: Int = 2) {
    this.connection.send(ClientboundSetTitlesPacket())
}