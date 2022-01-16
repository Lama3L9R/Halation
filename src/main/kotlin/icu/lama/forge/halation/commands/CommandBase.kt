package icu.lama.forge.halation.commands

import net.minecraft.server.level.ServerPlayer
import java.util.HashMap


/**
 * @author Qumolama.d
 * created on 1/13/2022
 * lama.icu | thelama.cn | lama3l9r.net | github.com/Lama3L9R
 */
interface CommandBase {
    //                                  Node  Description
    val registeredPermissions: HashMap<String, String>
    val baseName: String

    fun execute(sender: ServerPlayer, args: Array<String>)
}