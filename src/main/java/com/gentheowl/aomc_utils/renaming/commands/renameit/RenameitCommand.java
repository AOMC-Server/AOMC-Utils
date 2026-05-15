package com.gentheowl.aomc_utils.renaming.commands.renameit;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.commands.customize.CustomizeCommand;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import java.util.List;

public class RenameitCommand extends CommandRoot {
    private RenameitCommand() {}
    private static final RenameitCommand INSTANCE = new RenameitCommand();
    public static RenameitCommand getInstance() { return INSTANCE; }


    private static final String NAME = "renameit";

    @Override
    public String getRootName() {
        return NAME;
    }

    @Override
    public List<Subcommand> getSubcommands() {
        return List.of(
                new ModifySubcommand(),
                new MaxLoreSubcommand(),
                new FormatSubcommand(),
                new PermissionAPISubcommand(),
                new ReloadSubcommand()
        );
    }

    @Override
    protected boolean getRequiredPermissions(CommandSourceStack source) {
        return AOMCUtils.CONFIG.shouldUsePermissionsAPI()
                ? Permissions.check(source, "renameit.renameit")
                : Commands.hasPermission(Commands.LEVEL_ADMINS).test(source);
    }
}
