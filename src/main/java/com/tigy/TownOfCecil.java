package com.tigy;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

public class TownOfCecil implements ModInitializer {
    public static final String MOD_ID = "town_of_cecil";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Map<String, String> playerColors = new HashMap<>();

    @Override
    public void onInitialize() {
        // Register command callback
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("assigncolor")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                            .then(CommandManager.argument("color", StringArgumentType.word())
                                    .executes(ctx -> {
                                        String name = StringArgumentType.getString(ctx, "name");
                                        String color = StringArgumentType.getString(ctx, "color");
                                        playerColors.put(name, color);
                                        ctx.getSource().sendFeedback(() -> Text.literal("Assigned " + name + " to color ").append(coloredColorName(color)), false);
                                        return 1;
                                    })
                            )
                    )
            );

            dispatcher.register(literal("assignmetocolor")
                    .then(CommandManager.argument("color", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = ctx.getSource().getPlayer().getName().getString();
                                String color = StringArgumentType.getString(ctx, "color");
                                playerColors.put(name, color);
                                ctx.getSource().sendFeedback(() -> Text.literal("Assigned " + name + " to color ").append(coloredColorName(color)), false);

                                return 1;
                            })
                    )
            );

            dispatcher.register(literal("listcolors")
                    .executes(ctx -> {
                        if (playerColors.isEmpty()) {
                            ctx.getSource().sendFeedback(() -> Text.literal("No player colors assigned."), false);
                        } else {
                            List<MutableText> lines = new ArrayList<>();
                            lines.add(Text.literal("Player Colors:"));
                            for (Map.Entry<String, String> entry : playerColors.entrySet()) {
                                String name = entry.getKey();
                                String colorName = entry.getValue();
                                MutableText line = Text.literal(name + ": ")
                                        .append(coloredColorName(colorName));
                                lines.add(line);
                            }
                            MutableText result = lines.get(0);
                            for (int i = 1; i < lines.size(); i++) {
                                result = result.append(Text.literal("\n")).append(lines.get(i));
                            }
                            final MutableText finalResult = result.copy(); // Make result effectively final

                            // Send the color list
                            ctx.getSource().sendFeedback(() -> finalResult, false);

                            // Add a clickable "copy to clipboard" message
                            MutableText copyText = Text.literal("Click here to copy the color list")
                                    .styled(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(finalResult.getString())));

                            ctx.getSource().sendFeedback(() -> Text.literal("").append(copyText), false);
                        }
                        return 1;
                    })
            );
        });
    }

    private static MutableText coloredColorName(String colorName) {
        Formatting formatting;
        try {
            formatting = Formatting.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            formatting = Formatting.WHITE;
        }
        Formatting finalFormatting = formatting;
        return Text.literal(colorName).styled(style -> style.withColor(finalFormatting));
    }
}

