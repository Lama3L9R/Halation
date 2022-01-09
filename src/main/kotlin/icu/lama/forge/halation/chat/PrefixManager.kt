package icu.lama.forge.halation.chat

import icu.lama.forge.halation.HalationConfig
import icu.lama.forge.halation.HalationForge
import icu.lama.forge.halation.utils.doc
import java.util.*
import kotlin.concurrent.thread

// todo test
object PrefixManager {
    private val collection = HalationForge.mongoDatabase.getCollection("PlayerPrefix")
    private val caches = hashMapOf<UUID, String>()
    private val locked: List<UUID>

    init {
        collection.find().forEach {
            caches[UUID.fromString(it["uuid"] as String)] = it["prefix"] as String
        }

        locked = HalationConfig.Prefix.lockedPlayerPrefixUUID.get().map { UUID.fromString(it) }
    }

    operator fun get(uuid: UUID): String {
        return caches.getOrDefault(uuid, HalationConfig.Prefix.defaultPlayerPrefix.get())
    }

    operator fun get(uuid: String): String {
        return caches.getOrDefault(UUID.fromString(uuid), HalationConfig.Prefix.defaultPlayerPrefix.get())
    }

    operator fun set(uuid: UUID, new: String) {
        if(uuid in locked) {
            return
        }

        if(uuid !in caches) {
            thread {
                collection.insertOne(doc("""
                    {
                        "uuid": "$uuid",
                        "prefix": "$new",
                        "isLocked": false
                    }
                """.trimIndent()))
            }
        } else {
            thread {
                collection.updateOne(doc("""
                    {
                        "uuid": "$uuid"
                    }
                """.trimIndent()), doc("""
                    {
                        "${"\$set"}": {
                            "prefix": "$new"
                        }
                    }
                """.trimIndent()))
            }
        }
        caches[uuid] = new
    }
}