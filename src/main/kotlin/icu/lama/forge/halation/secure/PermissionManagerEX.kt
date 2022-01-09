package icu.lama.forge.halation.secure

import icu.lama.forge.halation.HalationConfig
import icu.lama.forge.halation.HalationForge
import icu.lama.forge.halation.utils.doc
import icu.lama.forge.halation.utils.potential
import icu.lama.forge.halation.utils.static
import icu.lama.forge.halation.utils.with
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.bson.Document
import java.util.*
import kotlin.concurrent.thread

// EX makes this class look cooler
// and also makes this stuff looks nicer
// (btw I pretty like bukkit plugin PEX's name)

/*
Data structure:
    PermissionNode {
        node: string | regex
        state: 'allowed' | 'restricted'
    }

    PlayerEntry {
        uuid: UUID
        permissions: PermissionNode[]
    }

    GroupEntry {
        name: string
        members: UUID[]
        permissions: PermissionNode[]
    }

    Collection PermissionEXPlayers: PlayerEntry
    Collection PermissionEXGroups: GroupEntry

Lazy for using data classes
mainly because 2 extra packages required(ktx.seri and ktx.bson)

permissions:
    interact.<block_id>
    chat.send
    chat.receive
    command.<category>.<command>.[<param1>.<param2>....]
    craft.<block_id>

default owned:
    interact.*
    chat.*
    command.halation.lm
    command.minecraft.tell
    command.minecraft.help
    craft.*
*/
object PermissionManagerEX {
    private val caches = hashMapOf<UUID, List<PermissionNode>>()
    private val playerPermissions = HalationForge.mongoDatabase.getCollection("PermissionEXPlayers")
    private val groupPermissions = HalationForge.mongoDatabase.getCollection("PermissionEXGroups")

    init {
        // cache data to make it faster
        HalationForge.logger.info("PEX -> Caching permissions")
        playerPermissions.find().forEach {
            caches[UUID.fromString(it["uuid", String::class.java])] = it.getList("permissions", PermissionNode::class.java)
        }

        groupPermissions.find().forEach { doc ->
            val permissions = doc.getList("permissions", PermissionNode::class.java)
            doc.getList("members", String::class.java).map { UUID.fromString(it) }.forEach {
                if(it !in caches) {
                    caches[it] = permissions
                } else {
                    caches[it] = caches[it]!! + permissions
                }
            }
        }
    }

    fun check(check: String, nodes: List<PermissionNode>): Boolean {
        return nodes.filter { match(check, it.node) }.run { this.none { !it.state } && this.isNotEmpty() }
    }

    private fun match(check: String, node: String): Boolean {
        return if(node.contains("*")) {
            !node.split(".").with(check.split(".")) { node, check ->
                if(node != null && check != null) {
                    node == check || node == "*"
                } else if(node == null && check != null) {
                    true
                } else node != null
            }.any { !it }

        } else {
            check == node
        }
    }

    // TODO test
    object Player {
        @SubscribeEvent @static fun onPlayerLogin(e: PlayerEvent.PlayerLoggedInEvent) {
            if(e.player.uuid !in caches) {
                val doc = Document()
                doc["permissions"] = HalationConfig.PermissionEX.defaults
                doc["uuid"] = e.player.uuid.toString()

                playerPermissions.insertOne(doc)
            }
        }

        fun grant(uuid: UUID, node: String) {
            if(uuid !in caches) {
                HalationForge.logger.potential("PermissionManagerEX.kt", "01nHMQ6w", "How is this even possible??? UUID = $uuid SizeOfCache = ${caches.size}")

                caches[uuid] = listOf(PermissionNode(node))

                thread {
                    playerPermissions.insertOne(doc("""
                    {
                        "uuid": $uuid,
                        "permissions": [{ "node": ${node}, "state": true }]
                    }
                """.trimIndent()))
                }
            } else {
                caches[uuid] = caches[uuid]!!.filter { it.node != node } + PermissionNode(node)

                thread {
                    // 不直接用$set的原因是因为$set不能保证原Entry的存在性，故不适用$set
                    playerPermissions.updateMany(doc("""
                    {
                        "uuid": $uuid
                    }
                """.trimIndent()), doc("""
                    [
                        {
                            "${"\$push"}": {
                                "permissions": { "node": "$node", "state": true }
                            }
                        },
                        {
                            "${"\$pull"}": {
                                "permissions": { "node": "$node", "state": false }
                            }
                        }
                    ]
                """.trimIndent()))
                }
            }
        }

        fun revoke(uuid: UUID, node: String) {
            if(uuid in caches) {
                caches[uuid] = caches[uuid]!!.filter { it.node != node }

                thread {
                    playerPermissions.updateOne(doc("""
                    {
                        "uuid": $uuid
                    }
                """.trimIndent()), doc("""
                    {
                        "${"\$pull"}": {
                            "permissions": { "node": "$node" }
                        }
                    }
                """.trimIndent()))
                }
            }
        }

        fun restrict(uuid: UUID, node: String) {
            if(uuid !in caches) {
                caches[uuid] = listOf(PermissionNode(node, false))

                thread {
                    playerPermissions.insertOne(doc("""
                    {
                        "uuid": $uuid,
                        "permissions": [{ "node": ${node}, "state": false }]
                    }
                """.trimIndent()))
                }
            } else {
                caches[uuid] = caches[uuid]!!.filter { it.node != node } + PermissionNode(node)

                thread {
                    playerPermissions.updateMany(doc("""
                    {
                        "uuid": $uuid
                    }
                """.trimIndent()), doc("""
                    [
                        {
                            "${"\$push"}": {
                                "permissions": { "node": "$node", "state": false }
                            }
                        },
                        {
                            "${"\$pull"}": {
                                "permissions": { "node": "$node", "state": true }
                            }
                        }
                    ]
                """.trimIndent()))
                }
            }
        }
    }

    object Group {
        // todo impl
        fun grant(name: String, node: String) {

        }

        fun create(name: String) {
            groupPermissions.insertOne(doc("""
                {
                    "name": "$name",
                    "members": [],
                    "permissions": []
                }
            """.trimIndent()))
        }

        fun list() {

        }

        fun delete(name: String) {

        }

        fun query(name: String) {

        }

        fun addMember(name: String, uuid: UUID) {

        }

        fun removeMember(name: String, uuid: UUID) {

        }

        fun revoke(name: String, node: String) {

        }

        fun restrict(name: String, node: String) {

        }
    }
}