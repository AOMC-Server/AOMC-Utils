package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class UnsignSubcommand implements Subcommand {
    private static final String NAME = ConfigSettings.UNSIGN;
    private static final String USAGE = "";
    private static final String DESC  = "Unlock a signed item to re-enable editing (only if you are the one who signed it!)";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> attach(CommandRoot parent) {
        return Commands.literal(NAME).requires(this::getRequiredPermission).executes(this::execute);
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    @SuppressWarnings("SameReturnValue")
    private int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        ItemModificationResult result = ItemManager.UNSIGN.validateAndRun(player, player);
        src.sendSuccess(result::getMessage, false);
        return 1;
    }

    @Override
    public boolean getRequiredPermission(CommandSourceStack src) {
        return !AOMCUtils.CONFIG.shouldUsePermissionsAPI() || Permissions.check(src, "renameit.customize.unsign");
    }
}