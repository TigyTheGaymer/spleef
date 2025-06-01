package com.tigy;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Optional;

public class SpecialItem {

    public static final String NAME = "Special Paper";

    public static ItemStack create() {
        // Create an ItemStack of vanilla paper
//        NbtCompound nbtCompound = new NbtCompound();
//        nbtCompound.putBoolean("Undroppable", true);
//        nbtCompound.putBoolean("OnlyOne", true);
//        NbtComponent nbtComponent = NbtComponent.of(nbtCompound);

        ItemStack paper = new ItemStack(Items.PAPER);
        paper.set(DataComponentTypes.ITEM_NAME, Text.literal(NAME).formatted(Formatting.BOLD).formatted(Formatting.GOLD));
//        paper.set(DataComponentTypes.CUSTOM_NAME, Text.literal(NAME).formatted(Formatting.BOLD).formatted(Formatting.GOLD));
//        paper.set(DataComponentTypes.CUSTOM_DATA, nbtComponent);
        paper.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("Tigy").formatted(Formatting.BOLD).formatted(Formatting.GOLD),
                Text.literal("Paper").formatted(Formatting.BOLD).formatted(Formatting.GOLD)
        )));

        return paper;
    }

    public static boolean equals(ItemStack stack) {
        var name = stack.get(DataComponentTypes.ITEM_NAME);
        if (name != null && name.getString().equals(NAME)) return true;

        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return false;

        NbtCompound data = component.copyNbt(); // Safely get the actual NBT compound

        return data.getBoolean("Undroppable").orElse(false);
    }

    public static void giveToPlayer(ServerPlayerEntity player) {

        // Try to insert the item into the player's inventory
        boolean hasAlreadyOne = player.getInventory().contains(SpecialItem::equals);

        if (!hasAlreadyOne) {
            player.getInventory().insertStack(SpecialItem.create());
            player.sendMessage(Text.literal("Special Paper received"), false);
        }
    }
}
