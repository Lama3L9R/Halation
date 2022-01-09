package icu.lama.forge.halation

import icu.lama.forge.halation.secure.PermissionNode
import icu.lama.forge.halation.utils.*

object HalationConfig {
    @static val configSpec: TOML
    @static val configBuilder = TOMLBuilder()

    init {
        configBuilder.push("MongoDB")
            Mongodb.mongodbURL = configBuilder.define("mongoURL", "mongodb://localhost:27017/?readPreference=primary&appname=MongoDB%20Compass&directConnection=true&ssl=false")
            Mongodb.database = configBuilder.define("database", "Halation")
        configBuilder.pop()

        configBuilder.push("PermissionEX")
            PermissionEX.defaults = configBuilder.define("defaults", arrayOf())
        configBuilder.pop()

        configBuilder.push("Prefix")
            Prefix.defaultPlayerPrefix = configBuilder.define("defaultPlayerPrefix", "${ChatColor.GREEN}良民")
            Prefix.lockedPlayerPrefixUUID = configBuilder.define("lockedPlayerPrefixUUID", arrayOf("56573303-d37e-3b5f-9e24-db6ba7f34f13", "3784a03c-59fc-447b-bc10-83a9f52b322f"))
        configBuilder.pop()

        configSpec = configBuilder.build()
    }

    object PermissionEX {
        // TODO object test
        @static lateinit var defaults: TOMLEntry<Array<PermissionNode>>
    }

    object Prefix {
        @static lateinit var defaultPlayerPrefix: TOMLEntry<String>
        @static lateinit var lockedPlayerPrefixUUID: TOMLEntry<Array<String>>
    }

    object Mongodb {
        @static lateinit var mongodbURL: TOMLEntry<String>
        @static lateinit var database: TOMLEntry<String>
    }
}
