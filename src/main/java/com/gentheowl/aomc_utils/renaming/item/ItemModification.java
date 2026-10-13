package com.gentheowl.aomc_utils.renaming.item;

import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;
import org.jetbrains.annotations.Nullable;
import parliament.api.entity.Player;
import parliament.api.item.ItemStack;

public class ItemModification<P> {
    @FunctionalInterface
    public interface ModificationAction<P> {
        ItemModificationResult apply(ItemStack stack, P params);
    }

    /** Items carrying this key in their custom data can be signed/unsigned/checked but never edited. */
    public static final String UNIQUE_TAG = "aomc_utils:unique";

    private final ModificationAction<P> action;
    private final String name;

    public ItemModification(String name, ModificationAction<P> action) {
        this.name = name;
        this.action = action;
    }

    public ItemModificationResult validateAndRun(@Nullable Player player, P params) {
        if (player == null) { return ItemModificationResult.PLAYER_ONLY; }
        if (!isEnabled(this.name)) { return ItemModificationResult.NOT_ENABLED; }
        ItemStack stack = player.mainHandItem();
        if (stack.isEmpty()) return ItemModificationResult.NO_ITEM_IN_HAND;
        if (isUnique(stack) && this != ItemManager.SIGN && this != ItemManager.UNSIGN) return ItemModificationResult.FAILURE_STACK_UNIQUE;
        if (!isSigned(stack) && this == ItemManager.UNSIGN) return ItemModificationResult.STACK_UNSIGNED;
        if (isSigned(stack) && this != ItemManager.UNSIGN)  return ItemModificationResult.FAILURE_ALREADY_SIGNED;

        int cost = getCost(name);
        if (player.experienceLevel() < cost) return ItemModificationResult.failure("You are missing the required levels ("+ cost +") for this action!");

        ItemModificationResult result = action.apply(stack, params);
        if (result.isSuccess()) {
            player.giveExperienceLevels(-cost);
        }
        return result;
    }

    private static boolean isSigned(ItemStack stack) {
        return stack.attachment().has("signed");
    }

    private static boolean isUnique(ItemStack stack) {
        return stack.attachment().has(UNIQUE_TAG);
    }

    private static int getCost(String key) {
        ConfigSettings.CommandEntry entry = RenameitConfig.get().command_data().get(key);
        return entry.cost();
    }

    private static boolean isEnabled(String key) {
        ConfigSettings.CommandEntry entry = RenameitConfig.get().command_data().get(key);
        return entry.enabled();
    }

}
