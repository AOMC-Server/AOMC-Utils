package com.gentheowl.aomc_utils.renaming;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ItemManager {
    private static final int MAX_LORE = LoreComponent.MAX_LORES;
    private static final int MODIFICATION_COST_LEVELS = 1;

    public static ItemModificationResult modifyName(ServerPlayerEntity player, Text name) {
        return withValidatedStack(player, stack -> {
            stack.set(DataComponentTypes.CUSTOM_NAME, name);
            return ItemModificationResult.success("Item name set successfully!");

        });
    }

    public static ItemModificationResult setLore(ServerPlayerEntity player, List<Text> lines) {
        return withValidatedStack(player, stack -> {
            stack.set(DataComponentTypes.LORE, new LoreComponent(lines));
            return ItemModificationResult.success("Lore set successfully!");
        });
    }

    public static ItemModificationResult pushLore(ServerPlayerEntity player, Text line) {
        return withValidatedStack(player, stack -> {
            LoreComponent lore = stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
            if (lore.lines().size() >= MAX_LORE) return ItemModificationResult.MAX_LORE_LINES;

            stack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, line, LoreComponent::with);
            return ItemModificationResult.success("Lore added successfully!");
        });
    }

    public static ItemModificationResult removeLoreLine(ServerPlayerEntity player, int index) {
        return withValidatedStack(player, stack -> {
            List<Text> lines = new ArrayList<>(stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines());
            if (index < 0 || index >= lines.size()) return ItemModificationResult.OUT_OF_BOUNDS;

            lines.remove(index);
            stack.set(DataComponentTypes.LORE, new LoreComponent(lines));
            return ItemModificationResult.success("Lore removed successfully!");
        });
    }

    public static ItemModificationResult insertLoreLine(ServerPlayerEntity player, int index, Text line) {
        return withValidatedStack(player, stack -> {
            List<Text> lines = new ArrayList<>(stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines());
            if (index < 0 || index > lines.size()) return ItemModificationResult.OUT_OF_BOUNDS;
            if (lines.size() >= MAX_LORE) return ItemModificationResult.MAX_LORE_LINES;

            lines.add(index, line);
            stack.set(DataComponentTypes.LORE, new LoreComponent(lines));
            return ItemModificationResult.success("Lore inserted successfully!");
        });
    }

    public static ItemModificationResult glint(ServerPlayerEntity player, boolean value) {
        return withValidatedStack(player, stack -> {
            stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, value);
            return ItemModificationResult.success("Glint " + (value ? "enabled" : "disabled") + " successfully!");
        });
    }

    public static ItemModificationResult sign(ServerPlayerEntity player, boolean withText) {
        return withValidatedStack(player, stack -> {
            if (withText) {
                pushLore(player, TextUtil.signature(player.getName().getString()));
            }
            NbtCompound tag = new NbtCompound();
            tag.putBoolean("signed", true);
            tag.putString("signer", player.getUuidAsString());
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
            return ItemModificationResult.success("You have signed this item! It can no longer be edited.");
        });
    }

    public static ItemModificationResult unsign(ServerPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) return ItemModificationResult.NO_ITEM_IN_HAND;
        if (!isSigned(stack)) return ItemModificationResult.STACK_UNSIGNED;
        if (missingLevels(player)) return ItemModificationResult.MISSING_LEVELS;
        if (!isPlayerSigner(player, stack)) return ItemModificationResult.WRONG_SIGNER;
        stack.remove(DataComponentTypes.CUSTOM_DATA);
        if (stack.contains(DataComponentTypes.LORE)) {
            LoreComponent lore = stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
            Deque<Text> original = new ArrayDeque<>(lore.lines());

            if (!original.isEmpty()) {
                Text last = original.peekLast();
                if (last.getStyle().getFont().equals(TextUtil.UNFIFORM_FONT)) {
                    original.removeLast();
                }
            }
            stack.set(DataComponentTypes.LORE, new LoreComponent(List.copyOf(original)));
        }

        return ItemModificationResult.success("The item has been unsigned and is now editable again.");
    }

    private static ItemModificationResult withValidatedStack(ServerPlayerEntity player, java.util.function.Function<ItemStack, ItemModificationResult> fn) {
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) return ItemModificationResult.NO_ITEM_IN_HAND;
        if (isSigned(stack)) return ItemModificationResult.STACK_SIGNED;
        if (missingLevels(player)) return ItemModificationResult.MISSING_LEVELS;

        ItemModificationResult result = fn.apply(stack);
        if (result.isSuccess()) {
            player.addExperienceLevels(-1);
        }
        return result;
    }

    private static boolean isSigned(ItemStack stack) {
        NbtComponent nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        return (nbt.contains("signed"));
    }

    private static boolean missingLevels(ServerPlayerEntity player) {
        return player.experienceLevel < MODIFICATION_COST_LEVELS;
    }

    private static boolean isPlayerSigner(ServerPlayerEntity player, ItemStack stack) {
        NbtComponent nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        if (!nbt.contains("signed")) return false;
        return nbt.copyNbt().getString("signer", "").equals(player.getUuidAsString());
    }
}
