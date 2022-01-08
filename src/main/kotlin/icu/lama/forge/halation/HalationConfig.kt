package icu.lama.forge.halation

import icu.lama.forge.halation.utils.TOML
import icu.lama.forge.halation.utils.TOMLBuilder
import icu.lama.forge.halation.utils.TOMLEntry
import icu.lama.forge.halation.utils.static

object HalationConfig {
    @static lateinit var configSpec: TOML
    @static val configBuilder = TOMLBuilder()

    init {
        configBuilder.push("MongoDB")

        Mongodb.mongodbURL = configBuilder.define("mongoURL", "mongodb://localhost:27017/?readPreference=primary&appname=MongoDB%20Compass&directConnection=true&ssl=false")
        Mongodb.database = configBuilder.define("database", "Halation")

        configBuilder.pop()
    }



    object Mongodb {
        @static lateinit var mongodbURL: TOMLEntry<String>
        @static lateinit var database: TOMLEntry<String>
    }
}
