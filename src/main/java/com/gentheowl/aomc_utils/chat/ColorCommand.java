package com.gentheowl.aomc_utils.chat;

import parliament.api.Parliament;
import parliament.api.command.Argument;
import parliament.api.command.Command;
import parliament.api.entity.Player;
import parliament.api.permission.PermissionLevel;
import parliament.api.text.Color;
import parliament.api.text.Message;

import java.util.List;

public final class ColorCommand {

    private ColorCommand() {}

    public static void register(Parliament parliament) {
        parliament.commands().register(
                Command.literal("color")
                        .child(Command.argument(Argument.ofWord("hex")).executesAlias(context -> {
                            Player player = context.player().orElse(null);

                            if (player == null || !isDonor(player)) {
                                context.sendFailure(Message.literal("You don't have permission to use /color."));
                                return List.of();
                            }
                            String hex = context.getText("hex");
                            context.sendSuccess(Message.literal("Chat color set to " + hex + ".").color(Color.GREEN));
                            return List.of("styledchat set " + player.name()
                                    + " message_formats.chat <color:" + hex + ">${player}</color:" + hex + "> ${message}");
                        })));
    }

    private static boolean isDonor(Player player) {
        return player.hasPermission("group.donor", PermissionLevel.ADMIN)
                || player.hasPermission("group.mod", PermissionLevel.ADMIN);
    }
}
