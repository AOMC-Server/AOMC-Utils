package com.gentheowl.aomc_utils.renaming.commands;

import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import java.util.List;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;
import parliament.api.permission.PermissionLevel;
import parliament.api.text.Color;
import parliament.api.text.Message;

public abstract class CommandRoot {
    public abstract String getRootName();
    public abstract List<Subcommand> getSubcommands();

    /** The permission node gating this root (resolved through the server's permission mod). */
    protected abstract String getPermissionNode();

    /** The operator tier that decides where no permission mod is installed. */
    protected abstract PermissionLevel getPermissionFallback();

    public void sendHelp(CommandContext ctx) {
        String root = this.getRootName();
        List<Subcommand> subcommands = this.getSubcommands();

        ctx.sendSuccess(TextUtil.header("/" + root + " Commands"));
        for (Subcommand sub : subcommands) {
            Message cmdLine = Message.literal("• ")
                    .append(Message.literal("/" + root + " " + sub.getName()).color(Color.AQUA))
                    .append(Message.literal(" " + sub.getUsage()).color(Color.DARK_AQUA));
            ctx.sendSuccess(cmdLine);

            Message descLine = Message.literal("    " + sub.getDescription())
                    .color(Color.GRAY)
                    .italic(true);
            ctx.sendSuccess(descLine);
        }
    }

    public Command register() {
        Command root = Command.literal(getRootName())
                .requiresPermission(getPermissionNode(), getPermissionFallback())
                .executes(this::sendHelp);

        for (Subcommand sub : getSubcommands()) {
            root.child(sub.attach(this));
        }

        return root;
    }
}
