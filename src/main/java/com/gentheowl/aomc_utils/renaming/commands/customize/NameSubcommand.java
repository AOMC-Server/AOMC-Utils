package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class NameSubcommand implements Subcommand {
    private static final String NAME = ConfigSettings.MODIFY_NAME;
    private static final String USAGE = "<text>";
    private static final String DESCRIPTION = "Sets the display name of the item in hand, supports formatting codes.";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> attach(CommandRoot parent) {
        return Commands.literal(NAME)
                .requires(this::getRequiredPermission)
                .executes(ctx -> { parent.sendHelp(ctx.getSource()); return 1; })
                .then(Commands.argument("text", StringArgumentType.greedyString())
                        .executes(this::executeSetName)
                );
    }

    @Override public String getName() { return NAME; }
    @Override public String getUsage() { return USAGE; }
    @Override public String getDescription() { return DESCRIPTION; }

    @SuppressWarnings("SameReturnValue")
    private int executeSetName(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();

        String raw = StringArgumentType.getString(ctx, "text");
        Component formatted = TextUtil.parse(raw, ctx.getSource());
        ItemModificationResult result = ItemManager.MODIFY_NAME.validateAndRun(player, formatted);
        ctx.getSource().sendSuccess(result::getMessage, false);
        return 1;
    }

    @Override
    public boolean getRequiredPermission(CommandSourceStack src) {
        return !AOMCUtils.CONFIG.shouldUsePermissionsAPI() || Permissions.check(src, "renameit.customize.name");
    }
}

