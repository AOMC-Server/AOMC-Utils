package com.gentheowl.aomc_utils.renaming.commands.renameit;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import java.util.List;
import parliament.api.permission.PermissionLevel;

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
    protected String getPermissionNode() {
        return "renameit.renameit";
    }

    @Override
    protected PermissionLevel getPermissionFallback() {
        // Matches the old vanilla op-level gate (Commands.LEVEL_ADMINS).
        return PermissionLevel.ADMIN;
    }
}
