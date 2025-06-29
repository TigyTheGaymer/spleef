package com.tigy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class Spleef implements ModInitializer {
    public static final String MOD_ID = "spleef";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private BlockPos corner1;
    private BlockPos corner2;

    @Override
    public void onInitialize() {

        // Register command callback
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("spleef")
                            .then(CommandManager.literal("build")
                                    .then(CommandManager.literal("corner1")
                                            .executes(ctx -> {
                                                corner1 = ctx.getSource().getPlayer().getBlockPos();
                                                ctx.getSource().sendFeedback(() -> Text.literal("Corner 1: " + corner1.toString()), false);
                                                return 1;
                                            })
                                    )
                                    .then(CommandManager.literal("corner2")
                                            .executes(ctx -> {
                                                corner2 = ctx.getSource().getPlayer().getBlockPos();
                                                ctx.getSource().sendFeedback(() -> Text.literal("Corner 1: " + corner2.toString()), false);
                                                fillWithSnow(ctx.getSource().getWorld(), corner1, corner2);
                                                return 1;
                                            })
                                    )
                            )
                            .then(CommandManager.literal("rebuild")
                                    .executes(ctx -> {
                                        if (corner1 == null || corner2 == null) {
                                            ctx.getSource().sendFeedback(() -> Text.literal("Corners not found"), false);
                                            return 1;
                                        }
                                        fillWithSnow(ctx.getSource().getWorld(), corner1, corner2);
                                        return 1;
                                    })
                            )
                            .then(CommandManager.literal("shovel")
                                    .executes(context -> {
                                        ServerPlayerEntity player = context.getSource().getPlayer();
                                        giveSpleefShovel(player);
                                        return 1;
                                    })
                            )


            );
        });
    }

    public static void fillWithSnow(ServerWorld world, BlockPos corner1, BlockPos corner2) {
        int minX = Math.min(corner1.getX(), corner2.getX());
        int minY = Math.min(corner1.getY(), corner2.getY());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());

        int maxX = Math.max(corner1.getX(), corner2.getX());
        int maxY = Math.max(corner1.getY(), corner2.getY());
        int maxZ = Math.max(corner1.getZ(), corner2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    world.setBlockState(pos, Blocks.SNOW_BLOCK.getDefaultState());
                }
            }
        }
    }

    private static void giveSpleefShovel(ServerPlayerEntity player) {
        DynamicRegistryManager registryManager = player.getWorld().getRegistryManager();

        ItemStack shovel = new ItemStack(Items.DIAMOND_SHOVEL);
        shovel.set(DataComponentTypes.ITEM_NAME, Text.literal("Spleef Shovel").formatted(Formatting.BOLD).formatted(Formatting.GOLD));
        shovel.set(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE);

        // Only allow breaking SNOW_BLOCK (Only works in adventure mode)
        RegistryEntryLookup<Block> blockRegistry = registryManager.getOrThrow(Registries.BLOCK.getKey());
        BlockPredicate predicate = BlockPredicate.Builder.create().blocks(blockRegistry, Blocks.SNOW_BLOCK).build();
        BlockPredicatesChecker checker = new BlockPredicatesChecker(List.of(predicate));
        shovel.set(DataComponentTypes.CAN_BREAK, checker);

        // Add EFFICIENCY Enchantment
        Registry<Enchantment> registry = registryManager.getOrThrow(Enchantments.EFFICIENCY.getRegistryRef());
        Enchantment enchantment = registry.get(Enchantments.EFFICIENCY);
        RegistryEntry<Enchantment> efficiencyEntry = RegistryEntry.of(enchantment);
        shovel.addEnchantment(efficiencyEntry, 69);

        // Give the shovel to the player
        player.giveItemStack(shovel);

        player.sendMessage(Text.literal("You've received a Spleef Shovel!"), false);
    }

}

