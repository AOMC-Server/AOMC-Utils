package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import java.util.List;
import parliament.api.permission.PermissionLevel;

public class CustomizeCommand extends CommandRoot {
    private CustomizeCommand() {}
    private static final CustomizeCommand INSTANCE = new CustomizeCommand();
    public static CustomizeCommand getInstance() { return INSTANCE; }

    private static final String ROOT = "customize";
    private static final List<Subcommand> SUBCOMMANDS = List.of(
            new NameSubcommand(),
            new LoreSubcommand(),
            new GlintSubcommand(),
            new SignSubcommand(),
            new UnsignSubcommand(),
            new CheckSubcommand()
    );

    @Override
    public String getRootName() {
        return ROOT;
    }

    @Override
    public List<Subcommand> getSubcommands() {
        return SUBCOMMANDS;
    }

    @Override
    protected String getPermissionNode() {
        return "renameit.customize";
    }

    @Override
    protected PermissionLevel getPermissionFallback() {
        return PermissionLevel.EVERYONE;
    }
}
