package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import java.util.List;

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
            new UnsignSubcommand()
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
    protected boolean getRequiredPermissions(CommandSourceStack source) {
        return !AOMCUtils.CONFIG.shouldUsePermissionsAPI() || Permissions.check(source, "renameit.customize");
    }
}
