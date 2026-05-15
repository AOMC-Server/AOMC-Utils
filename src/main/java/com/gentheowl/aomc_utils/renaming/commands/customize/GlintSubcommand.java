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

@SuppressWarnings("ALL")
public class GlintSubcommand implements Subcommand {
    private static final String NAME = ConfigSettings.GLINT;
    private static final String USAGE = "<true | false>";
    private static final String DESC  = "Toggle enchantment glint effect on the item in hand.";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> attach(CommandRoot parent) {
        return Commands.literal(NAME)
                .requires(this::getRequiredPermission)
                .executes(ctx -> { parent.sendHelp(ctx.getSource()); return 1; })
                .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(this::execute)
                );
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        boolean shouldGlint = BoolArgumentType.getBool(ctx, "value");
        ItemModificationResult result = ItemManager.GLINT.validateAndRun(player, shouldGlint);
        src.sendSuccess(result::getMessage, false);
        return 1;
    }

    @Override
    public boolean getRequiredPermission(CommandSourceStack src) {
        return !AOMCUtils.CONFIG.shouldUsePermissionsAPI() || Permissions.check(src, "renameit.customize.glint");
    }
}
