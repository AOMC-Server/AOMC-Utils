package com.gentheowl.aomc_utils.renaming.item;

import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import net.minecraft.network.chat.Component;

public class ItemModificationResult {
    private final boolean success;
    private final Component message;

    public static final ItemModificationResult NO_ITEM_IN_HAND = ItemModificationResult.failure("No item in hand.");
    public static final ItemModificationResult MAX_LORE_LINES = ItemModificationResult.failure("You cannot add any more lines of lore!");
    public static final ItemModificationResult OUT_OF_BOUNDS = ItemModificationResult.failure("The position you want to modify is either below 0 or larger than the amount of lines!");
    public static final ItemModificationResult PLAYER_ONLY = ItemModificationResult.failure("This can only be run by players.");
    public static final ItemModificationResult GENERIC_FAIL = ItemModificationResult.failure("Something seems to have gone wrong here.");
    public static final ItemModificationResult STACK_SIGNED = ItemModificationResult.failure("This item is signed and can no longer be edited.");
    public static final ItemModificationResult STACK_UNSIGNED = ItemModificationResult.failure("This item can't be unsigned because it's not signed.");
    public static final ItemModificationResult WRONG_SIGNER = ItemModificationResult.failure("You are not the original signer!");
    public static final ItemModificationResult NOT_ENABLED = ItemModificationResult.failure("This command is disabled.");

    public static final ItemModificationResult GENERAL_SUCCESS = ItemModificationResult.success("Successfully applied changes.");

    private ItemModificationResult(boolean success, Component message) {
        this.success = success;
        this.message = message;
    }

    public static ItemModificationResult success(Component message) {
        return new ItemModificationResult(true, message);
    }

    public static ItemModificationResult success(String message) {
        return new ItemModificationResult(true, TextUtil.success(message));
    }

    public static ItemModificationResult failure(Component message) {
        return new ItemModificationResult(false, message);
    }

    public static ItemModificationResult failure(String message) {
        return new ItemModificationResult(true, TextUtil.error(message));
    }

    public boolean isSuccess() {
        return success;
    }

    public Component getMessage() {
        return message;
    }
}
