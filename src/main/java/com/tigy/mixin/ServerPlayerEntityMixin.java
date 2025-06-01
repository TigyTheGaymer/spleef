package com.tigy.mixin;

import com.tigy.SpecialItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    // Deny Player from dropping Special Item
    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    public void dropItem(ItemStack stack, boolean dropAtSelf, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if (SpecialItem.equals(stack)) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            // Cancel the drop
            cir.setReturnValue(null);

            player.setStackInHand(player.getActiveHand(), stack.copy());
        }
    }


}
