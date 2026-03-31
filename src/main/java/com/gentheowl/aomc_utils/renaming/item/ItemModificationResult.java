package com.gentheowl.aomc_utils.renaming;

import net.minecraft.item.Item;
import net.minecraft.text.Text;

public class ItemModificationResult {
    private final boolean success;
    private final Text message;

    public static ItemModificationResult NO_ITEM_IN_HAND = ItemModificationResult.failure("No item in hand.");
    public static ItemModificationResult MAX_LORE_LINES = ItemModificationResult.failure("You cannot add any more lines of lore!");
    public static ItemModificationResult OUT_OF_BOUNDS = ItemModificationResult.failure("The position you want to modify is either below 0 or larger than the amount of lines!");
    public static ItemModificationResult PLAYER_ONLY = ItemModificationResult.failure("This can only be run by players.");
    public static ItemModificationResult GENERIC_FAIL = ItemModificationResult.failure("Something seems to have gone wrong here.");
    public static ItemModificationResult STACK_SIGNED = ItemModificationResult.failure("This item is signed and can no longer be edited.");
    public static ItemModificationResult STACK_UNSIGNED = ItemModificationResult.failure("This item can't be unsigned because it's not signed.");
    public static ItemModificationResult WRONG_SIGNER = ItemModificationResult.failure("You are not the original signer!");
    public static ItemModificationResult MISSING_LEVELS = ItemModificationResult.failure("You are missing the required levels for this action!");

    private ItemModificationResult(boolean success, Text message) {
        this.success = success;
        this.message = message;
    }

    public static ItemModificationResult success(Text message) {
        return new ItemModificationResult(true, message);
    }

    public static ItemModificationResult success(String message) {
        return new ItemModificationResult(true, TextUtil.success(message));
    }

    public static ItemModificationResult failure(Text message) {
        return new ItemModificationResult(false, message);
    }

    public static ItemModificationResult failure(String message) {
        return new ItemModificationResult(true, TextUtil.error(message));
    }

    public boolean isSuccess() {
        return success;
    }

    public Text getMessage() {
        return message;
    }
}
