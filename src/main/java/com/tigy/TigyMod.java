package com.tigy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TigyMod implements ModInitializer {
    public static final String MOD_ID = "tigy_mod";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
            ServerPlayerEntity player = serverPlayNetworkHandler.getPlayer();
//            player.sendMessage(Text.literal("Welcome to the server!").formatted(Formatting.BOLD), true);
            SpecialItem.giveToPlayer(player);
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            for (int i = 0; i < oldPlayer.getInventory().size(); i++) {
                ItemStack stack = oldPlayer.getInventory().getStack(i);

                if (!stack.isEmpty() && SpecialItem.equals(stack)) {
                    // On death since we keep the item with PlayerInventoryMixin (death)
                    // we also want to copy the item to the new player (respawn)
                    newPlayer.getInventory().setStack(i, stack.copy());
                }
            }
        });
    }
}