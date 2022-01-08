package icu.lama.forge.halation.inject;

import icu.lama.forge.halation.HalationForge;
import icu.lama.forge.halation.chat.ServerChatNetworkHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerGamePacketListenerImpl.class) public class MinecraftChatHandler {
    @Shadow public ServerPlayer player;
    @Final @Shadow private MinecraftServer server;
    @Shadow private int chatSpamTickCount;


    /**
     * @author Qumolama
     * @reason NO REASON 懂得都懂，不懂得说了也不懂
     */
    @Overwrite private void handleChat(String msg) {
        HalationForge.INSTANCE.getLogger().info("Handling chat for player: " + player.getName() + " message: " + msg);

        if(player.getChatVisibility() == ChatVisiblity.HIDDEN) {
            send(new ClientboundChatPacket((new TranslatableComponent("chat.cannotSend")).withStyle(ChatFormatting.RED), ChatType.SYSTEM, Util.NIL_UUID));
        } else {
            player.resetLastActionTime();

            if(!msg.chars().mapToObj((it) -> (char) it).allMatch(SharedConstants::isAllowedChatCharacter)) {
                disconnect(new TranslatableComponent("multiplayer.disconnect.illegal_characters"));
            }

            if(msg.startsWith("/")) {
                handleCommand(msg);
            } else {
                Component comp = ServerChatNetworkHandler.INSTANCE.handle(player, msg);
                comp = ForgeHooks.onServerChatEvent((ServerGamePacketListenerImpl)((Object) this), msg, comp);

                HalationForge.INSTANCE.getLogger().debug("debug comp(event launched) = " + comp);

                server.getPlayerList().broadcastMessage(comp, ChatType.CHAT, player.getUUID());
            }

            chatSpamTickCount += 20;
            if(chatSpamTickCount >= 200 && !server.getPlayerList().isOp(player.getGameProfile())) {
                this.disconnect(new TranslatableComponent("disconnect.spam"));
            }
        }

        return;
    }

    @Shadow public void send(Packet<?> p_147359_1_) {
        throw new NotImplementedException("How is this even possible?");
    }

    @Shadow public void disconnect(Component p_194028_1_ ) {
        throw new NotImplementedException("How is this even possible?");
    }

    @Shadow private void handleCommand(String p_147361_1_) {
        throw new NotImplementedException("How is this even possible?");
    }
}
