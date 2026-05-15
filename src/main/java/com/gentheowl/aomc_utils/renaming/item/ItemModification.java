package com.gentheowl.aomc_utils.renaming.item;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

public class ItemModification<P> {
    @FunctionalInterface
    public interface ModificationAction<P> {
        ItemModificationResult apply(ItemStack stack, P params);
    }

    private final ModificationAction<P> action;
    private final String name;

    public ItemModification(String name, ModificationAction<P> action) {
        this.name = name;
        this.action = action;
    }

    public ItemModificationResult validateAndRun(@Nullable ServerPlayer player, P params) {
        if (player == null) { return ItemModificationResult.PLAYER_ONLY; }
        if (!isEnabled(this.name)) { return ItemModificationResult.NOT_ENABLED; }
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return ItemModificationResult.NO_ITEM_IN_HAND;
        if (!isSigned(stack) && this == ItemManager.UNSIGN) return ItemModificationResult.STACK_UNSIGNED;
        if (isSigned(stack) && this != ItemManager.UNSIGN)  return ItemModificationResult.STACK_SIGNED;

        int cost = getCost(name);
        if (player.experienceLevel < cost) return ItemModificationResult.failure("You are missing the required levels ("+ cost +") for this action!");

        ItemModificationResult result = action.apply(stack, params);
        if (result.isSuccess()) {
            player.giveExperienceLevels(-cost);
        }
        return result;
    }

    private static boolean isSigned(ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return nbt.contains("signed");
    }

    private static int getCost(String key) {
        ConfigSettings.CommandEntry entry = AOMCUtils.CONFIG.command_data().get(key);
        return entry.cost();
    }

    private static boolean isEnabled(String key) {
        ConfigSettings.CommandEntry entry = AOMCUtils.CONFIG.command_data().get(key);
        return entry.enabled();
    }

}
