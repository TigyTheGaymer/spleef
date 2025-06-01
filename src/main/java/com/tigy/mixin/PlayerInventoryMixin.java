package com.tigy.mixin;

import com.tigy.SpecialItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    // Skip dropping Special Item on death
    @Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
    private void dropAll(CallbackInfo ci) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        PlayerEntity player = inventory.player;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);

            if (!stack.isEmpty()) {
                if (SpecialItem.equals(stack)) {
                    // Skip dropping this item on death
                    continue;
                }

                // Drop all other items normally
                player.dropItem(stack, true, false);
                inventory.setStack(i, ItemStack.EMPTY);
            }
        }

        // Cancel the original dropAll so it doesn’t drop again
        ci.cancel();
    }
}
