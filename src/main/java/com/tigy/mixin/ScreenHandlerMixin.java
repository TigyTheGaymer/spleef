package com.tigy.mixin;

import com.tigy.SpecialItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    // Deny insert Special Item into boxes (chest, shulke etc.) with shift click
    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast, CallbackInfoReturnable<Boolean> cir) {
        if (SpecialItem.equals(stack)) {
            cir.setReturnValue(false);  // cancel the insertion
        }
    }

    // Deny insert Special Item into boxes (chest, shulke etc.) with placing it with cursor
    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo cir) {
        // Ensure valid slot index
        if (slotIndex < 0 || slotIndex >= ((ScreenHandler)(Object)this).slots.size()) return;

        ScreenHandler handler = (ScreenHandler)(Object)this;
        Slot slot = handler.slots.get(slotIndex);

        // Item the player is trying to insert (usually from cursor)
        ItemStack cursorStack = player.currentScreenHandler.getCursorStack();

        // Skip if the item is not special
        if (!SpecialItem.equals(cursorStack)) return;

        // Don't block placing it into the player's own inventory
        if (slot != null && slot.inventory == player.getInventory()) return;

        // Block insertion into any other inventory (chest, shulker box, etc.)
        cir.cancel(); // Cancel the action
    }

}
