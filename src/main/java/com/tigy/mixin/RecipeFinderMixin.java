package com.tigy.mixin;

import com.tigy.SpecialItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeFinder.class)
public class RecipeFinderMixin {

    // Deny insert Special Item into crafting even with crafting book
    @Inject(method = "addInputIfUsable", at = @At("HEAD"), cancellable = true)
    private void addInputIfUsable(ItemStack item, CallbackInfo ci) {
        if (SpecialItem.equals(item)) {
            ci.cancel();
        }
    }
}