package icu.lama.forge.halation.utils

import icu.lama.forge.halation.HalationForge
import icu.lama.forge.halation.secure.PermissionManagerEX
import org.apache.logging.log4j.Logger
import org.bson.BsonDocument
import org.bson.Document

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

fun Logger.potential(module: String, id: String, msg: String) {
    this.warn("Potential Bug Report from '${module}' | ID = ${id}: $msg")
}