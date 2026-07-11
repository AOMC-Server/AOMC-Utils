package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;
import java.util.UUID;

public class CheckSubcommand implements Subcommand {
    private static final String NAME = "check";
    private static final String USAGE = "";
    private static final String DESC  = "Check whether the held item is genuinely signed, and by whom.";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> attach(CommandRoot parent) {
        return Commands.literal(NAME)
                .requires(this::getRequiredPermission)
                .executes(this::execute);
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(TextUtil.error("This can only be run by players."));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            src.sendFailure(TextUtil.error("No item in hand."));
            return 0;
        }

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("signed")) {
            src.sendSuccess(() -> TextUtil.info(
                    "This item is not signed."), false);
            return 1;
        }

        String signerUuid = tag.getStringOr("signer", "");
        String signerName = resolveSignerName(src.getServer(), signerUuid, tag.getStringOr("signer_name", ""));

        src.sendSuccess(() -> TextUtil.success("This item is signed."), false);
        src.sendSuccess(() -> detailLine("Signer", signerName), false);
        src.sendSuccess(() -> detailLine("UUID", signerUuid.isEmpty() ? "unknown" : signerUuid), false);
        return 1;
    }

    /** Online player > server name cache > name stored on the item at signing time. */
    private static String resolveSignerName(MinecraftServer server, String uuidString, String storedName) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return storedName.isEmpty() ? "unknown" : storedName;
        }

        ServerPlayer online = server.getPlayerList().getPlayer(uuid);
        if (online != null) return online.getName().getString();

        Optional<NameAndId> cached = server.services().nameToIdCache().get(uuid);
        if (cached.isPresent()) return cached.get().name();

        return storedName.isEmpty() ? "unknown" : storedName;
    }

    private static Component detailLine(String label, String value) {
        return Component.literal("  " + label + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }

    @Override
    public boolean getRequiredPermission(CommandSourceStack src) {
        return !AOMCUtils.CONFIG.shouldUsePermissionsAPI() || Permissions.check(src, "renameit.customize.check");
    }
}
