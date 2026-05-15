package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class SignSubcommand implements Subcommand {
    private static final String NAME = ConfigSettings.SIGN;
    private static final String USAGE = "<true | false>";
    private static final String DESC  = "Sign the item so it can no longer be edited.";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> attach(CommandRoot parent) {
        return Commands.literal(NAME)
                .requires(this::getRequiredPermission)
                .executes(ctx -> { parent.sendHelp(ctx.getSource()); return 1; })
                .then(Commands.argument("withText", BoolArgumentType.bool())
                        .executes(this::execute)
                );
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    @SuppressWarnings("SameReturnValue")
    private int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        boolean withText = BoolArgumentType.getBool(ctx, "withText");
        ItemModificationResult result = ItemManager.SIGN.validateAndRun(player, new ItemManager.SignParams(player, withText));
        src.sendSuccess(result::getMessage, false);
        return 1;
    }

    @Override
    public boolean getRequiredPermission(CommandSourceStack src) {
        return !AOMCUtils.CONFIG.shouldUsePermissionsAPI() || Permissions.check(src, "renameit.customize.sign");
    }
}
