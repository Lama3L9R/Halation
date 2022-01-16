package icu.lama.forge.halation.commands

import net.minecraft.server.level.ServerPlayer


/**
 * @author Qumolama.d
 * created on 1/13/2022
 * lama.icu | thelama.cn | lama3l9r.net | github.com/Lama3L9R
 */
interface CommandBase {
    fun execute(sender: ServerPlayer, args: Array<String>)
}