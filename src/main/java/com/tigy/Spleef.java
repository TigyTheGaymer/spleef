package com.tigy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.util.math.Vec3d;
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
                                                ctx.getSource().sendFeedback(() -> Text.literal("Corner 2: " + corner2.toString()), false);
                                                buildArena(ctx.getSource().getWorld(), corner1, corner2);
                                                return 1;
                                            })
                                    )
                            )
                            .then(CommandManager.literal("prebuild")
                                    .executes(ctx -> {
                                        buildPrebuildArena(ctx.getSource().getWorld(), ctx.getSource().getPlayer());
                                        return 1;
                                    })
                            )
                            .then(CommandManager.literal("rebuild")
                                    .executes(ctx -> {
                                        if (corner1 == null || corner2 == null) {
                                            ctx.getSource().sendFeedback(() -> Text.literal("Corners not found"), false);
                                            return 1;
                                        }
                                        buildArena(ctx.getSource().getWorld(), corner1, corner2);
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

    public static void buildPrebuildArena(ServerWorld world, ServerPlayerEntity player) {
        BlockPos startPos = player.getBlockPos();
        Vec3d lookVec = player.getRotationVec(1.0F).normalize();

        int length = 35; // forward
        int width = 17;  // perpendicular

        // Calculate the forward vector on XZ plane (ignore Y)
        Vec3d forward = new Vec3d(lookVec.x, 0, lookVec.z).normalize();

        // Calculate perpendicular vector (to the right) on XZ plane
        Vec3d right = new Vec3d(forward.z, 0, -forward.x).normalize();

        // Calculate corner1 and corner2 positions
        // We'll build the arena so that startPos is at one corner (bottom-left of arena)
        // corner1 = startPos shifted half width to the left (negative right), at start Y
        // corner2 = startPos shifted forward + half width to the right

        // Shift startPos left by half width
        Vec3d corner1Vec = new Vec3d(
                startPos.getX() + right.x * (-width / 2.0),
                startPos.getY(),
                startPos.getZ() + right.z * (-width / 2.0)
        );

        // Shift corner1Vec by length forward + width right
        Vec3d corner2Vec = new Vec3d(
                corner1Vec.x + forward.x * (length - 1) + right.x * (width - 1),
                corner1Vec.y,
                corner1Vec.z + forward.z * (length - 1) + right.z * (width - 1)
        );

        // Convert to BlockPos (floor values)
        BlockPos corner1 = new BlockPos(
                (int) Math.floor(corner1Vec.x),
                (int)corner1Vec.y,
                (int) Math.floor(corner1Vec.z)
        );

        BlockPos corner2 = new BlockPos(
                (int) Math.floor(corner2Vec.x),
                (int)corner2Vec.y,
                (int) Math.floor(corner2Vec.z)
        );

        // Call your buildArena function
        buildArena(world, corner1, corner2);
    }

    public static void buildArena(ServerWorld world, BlockPos corner1, BlockPos corner2) {
        int minX = Math.min(corner1.getX(), corner2.getX());
        int minY = Math.min(corner1.getY(), corner2.getY());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());

        int maxX = Math.max(corner1.getX(), corner2.getX());
        int maxY = Math.max(corner1.getY(), corner2.getY());
        int maxZ = Math.max(corner1.getZ(), corner2.getZ());

        int lavaY = minY - 3;          // Lava layer Y
        int glassBottomY = minY - 3;   // 1 blocks below lava
        int wallTopY = minY + 3;       // 3 blocks above snow level
        int floorY = lavaY - 1;        // Floor glass layer, right below lava

        // 1. Fill snow + air + lava inside volume
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockPos below1 = pos.down(1);
                    BlockPos below2 = pos.down(2);
                    BlockPos lavaPos = pos.down(3);

                    world.setBlockState(pos, Blocks.SNOW_BLOCK.getDefaultState(), 3);
                    world.setBlockState(below1, Blocks.AIR.getDefaultState(), 3);
                    world.setBlockState(below2, Blocks.AIR.getDefaultState(), 3);
                    world.setBlockState(lavaPos, Blocks.LAVA.getDefaultState(), 3);
                }
            }
        }

        // 2. Build glass walls from glassBottomY up to wallTopY (perimeter only)
        for (int x = minX - 1; x <= maxX + 1; x++) {
            for (int z = minZ - 1; z <= maxZ + 1; z++) {
                boolean isWallX = (x == minX - 1 || x == maxX + 1);
                boolean isWallZ = (z == minZ - 1 || z == maxZ + 1);

                if (isWallX || isWallZ) {
                    for (int y = glassBottomY; y <= wallTopY; y++) {
                        BlockPos wallPos = new BlockPos(x, y, z);
                        world.setBlockState(wallPos, Blocks.GLASS.getDefaultState(), 3);
                    }
                }
            }
        }

        // 3. Build glass floor underneath lava, full area (including inside walls)
        for (int x = minX - 1; x <= maxX + 1; x++) {
            for (int z = minZ - 1; z <= maxZ + 1; z++) {
                BlockPos floorPos = new BlockPos(x, floorY, z);
                world.setBlockState(floorPos, Blocks.GLASS.getDefaultState(), 3);
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

