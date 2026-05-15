package com.gentheowl.aomc_utils.renaming.item;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;
import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

public class ItemManager {
    public static final ItemModification<Component> MODIFY_NAME = new ItemModification<>(ConfigSettings.MODIFY_NAME, ItemManager::modifyName);
    public static final ItemModification<List<Component>> SET_LORE = new ItemModification<>(ConfigSettings.SET_LORE, ItemManager::setLore);
    public static final ItemModification<Component> PUSH_LORE = new ItemModification<>(ConfigSettings.PUSH_LORE, ItemManager::pushLore);
    public static final ItemModification<Integer> REMOVE_LORE_LINE = new ItemModification<>(ConfigSettings.REMOVE_LORE, ItemManager::removeLoreLine);
    public static final ItemModification<InsertLoreParams> INSERT_LORE_LINE = new ItemModification<>(ConfigSettings.INSERT_LORE, ItemManager::insertLoreLine);
    public static final ItemModification<Boolean> GLINT = new ItemModification<>(ConfigSettings.GLINT, ItemManager::glint);
    public static final ItemModification<SignParams> SIGN = new ItemModification<>(ConfigSettings.SIGN, ItemManager::sign);
    public static final ItemModification<ServerPlayer> UNSIGN = new ItemModification<>(ConfigSettings.UNSIGN, ItemManager::unsign);

    private static ItemModificationResult modifyName(ItemStack stack, Component name) {
        stack.set(DataComponents.CUSTOM_NAME, name);
        return ItemModificationResult.success("Item name set successfully!");
    }

    private static ItemModificationResult setLore(ItemStack stack, List<Component> lines) {
        stack.set(DataComponents.LORE, new ItemLore(lines));
        return ItemModificationResult.success("Lore set successfully!");
    }

    private static ItemModificationResult pushLore(ItemStack stack, Component line) {
        ItemLore lore = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        if (lore.lines().size() >= RenameitConfig.get().max_lore_lines()) return ItemModificationResult.MAX_LORE_LINES;

        stack.update(DataComponents.LORE, ItemLore.EMPTY, line, ItemLore::withLineAdded);
        return ItemModificationResult.success("Lore added successfully!");
    }

    private static ItemModificationResult removeLoreLine(ItemStack stack, @Nullable Integer index) {
        List<Component> lines = new ArrayList<>(stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines());
        if (lines.isEmpty()) return ItemModificationResult.failure("There is no lore to remove.");

        if (index == null) { index = lines.size() - 1; }
        if (index < 0 || index >= lines.size()) return ItemModificationResult.OUT_OF_BOUNDS;

        lines.remove(index.intValue());
        stack.set(DataComponents.LORE, new ItemLore(lines));
        return ItemModificationResult.success("Lore removed successfully!");
    }

    public record InsertLoreParams(Integer index, Component line){}
    private static ItemModificationResult insertLoreLine(ItemStack stack, InsertLoreParams params) {
        List<Component> lines = new ArrayList<>(stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines());
        if (params.index < 0 || params.index > lines.size()) return ItemModificationResult.OUT_OF_BOUNDS;
        if (lines.size() >= RenameitConfig.get().max_lore_lines()) return ItemModificationResult.MAX_LORE_LINES;

        lines.add(params.index, params.line);
        stack.set(DataComponents.LORE, new ItemLore(lines));
        return ItemModificationResult.success("Lore inserted successfully!");
    }

    private static ItemModificationResult glint(ItemStack stack, boolean value) {
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, value);
        return ItemModificationResult.success("Glint " + (value ? "enabled" : "disabled") + " successfully!");
    }

    public record SignParams(ServerPlayer player, Boolean withText) {}
    private static ItemModificationResult sign(ItemStack stack, SignParams params) {
        if (params.withText) {
            pushLore(stack, TextUtil.signature(params.player.getName().getString()));
        }
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("signed", true);
        tag.putString("signer", params.player.getStringUUID());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return ItemModificationResult.success("You have signed this item! It can no longer be edited.");
    }

    private static ItemModificationResult unsign(ItemStack stack, ServerPlayer player) {
        if (!isPlayerSigner(player, stack)) return ItemModificationResult.WRONG_SIGNER;
        stack.remove(DataComponents.CUSTOM_DATA);
        if (stack.has(DataComponents.LORE)) {
            ItemLore lore = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
            Deque<Component> original = new ArrayDeque<>(lore.lines());

            if (!original.isEmpty()) {
                Component last = original.peekLast();
                if (last.getStyle().getFont().equals(TextUtil.UNFIFORM_FONT)) {
                    original.removeLast();
                }
            }
            stack.set(DataComponents.LORE, new ItemLore(List.copyOf(original)));
        }

        return ItemModificationResult.success("The item has been unsigned and is now editable again.");
    }

    private static boolean isPlayerSigner(ServerPlayer player, ItemStack stack) {
        CustomData nbtComp = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag nbt = nbtComp.copyTag();
        if (!nbt.contains("signed")) return false;
        return nbt.getStringOr("signer", "").equals(player.getStringUUID());
    }
}
