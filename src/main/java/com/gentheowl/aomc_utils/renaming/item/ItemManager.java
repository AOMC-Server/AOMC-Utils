package com.gentheowl.aomc_utils.renaming.item;

import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;
import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import parliament.api.data.Attachment;
import parliament.api.entity.Player;
import parliament.api.item.ItemStack;
import parliament.api.text.Message;

public class ItemManager {
    public static final ItemModification<Message> MODIFY_NAME = new ItemModification<>(ConfigSettings.MODIFY_NAME, ItemManager::modifyName);
    public static final ItemModification<List<Message>> SET_LORE = new ItemModification<>(ConfigSettings.SET_LORE, ItemManager::setLore);
    public static final ItemModification<Message> PUSH_LORE = new ItemModification<>(ConfigSettings.PUSH_LORE, ItemManager::pushLore);
    public static final ItemModification<Integer> REMOVE_LORE_LINE = new ItemModification<>(ConfigSettings.REMOVE_LORE, ItemManager::removeLoreLine);
    public static final ItemModification<InsertLoreParams> INSERT_LORE_LINE = new ItemModification<>(ConfigSettings.INSERT_LORE, ItemManager::insertLoreLine);
    public static final ItemModification<Boolean> GLINT = new ItemModification<>(ConfigSettings.GLINT, ItemManager::glint);
    public static final ItemModification<SignParams> SIGN = new ItemModification<>(ConfigSettings.SIGN, ItemManager::sign);
    public static final ItemModification<Player> UNSIGN = new ItemModification<>(ConfigSettings.UNSIGN, ItemManager::unsign);

    private static ItemModificationResult modifyName(ItemStack stack, Message name) {
        stack.setCustomName(name);
        return ItemModificationResult.success("Item name set successfully!");
    }

    private static ItemModificationResult setLore(ItemStack stack, List<Message> lines) {
        stack.setLore(lines);
        return ItemModificationResult.success("Lore set successfully!");
    }

    private static ItemModificationResult pushLore(ItemStack stack, Message line) {
        List<Message> lines = new ArrayList<>(stack.lore());
        if (lines.size() >= RenameitConfig.get().max_lore_lines()) return ItemModificationResult.MAX_LORE_LINES;

        lines.add(line);
        stack.setLore(lines);
        return ItemModificationResult.success("Lore added successfully!");
    }

    private static ItemModificationResult removeLoreLine(ItemStack stack, @Nullable Integer index) {
        List<Message> lines = new ArrayList<>(stack.lore());
        if (lines.isEmpty()) return ItemModificationResult.failure("There is no lore to remove.");

        if (index == null) { index = lines.size() - 1; }
        if (index < 0 || index >= lines.size()) return ItemModificationResult.OUT_OF_BOUNDS;

        lines.remove(index.intValue());
        stack.setLore(lines);
        return ItemModificationResult.success("Lore removed successfully!");
    }

    public record InsertLoreParams(Integer index, Message line){}
    private static ItemModificationResult insertLoreLine(ItemStack stack, InsertLoreParams params) {
        List<Message> lines = new ArrayList<>(stack.lore());
        if (params.index < 0 || params.index > lines.size()) return ItemModificationResult.OUT_OF_BOUNDS;
        if (lines.size() >= RenameitConfig.get().max_lore_lines()) return ItemModificationResult.MAX_LORE_LINES;

        lines.add(params.index, params.line);
        stack.setLore(lines);
        return ItemModificationResult.success("Lore inserted successfully!");
    }

    private static ItemModificationResult glint(ItemStack stack, boolean value) {
        stack.setGlintOverride(value);
        return ItemModificationResult.success("Glint " + (value ? "enabled" : "disabled") + " successfully!");
    }

    public record SignParams(Player player, Boolean withText) {}
    private static ItemModificationResult sign(ItemStack stack, SignParams params) {
        if (params.withText) {
            pushLore(stack, TextUtil.signature(params.player.name()));
        }
        // Merge into existing custom data so unrelated tags (e.g. the unique marker) survive signing.
        stack.mergeAttachment(Attachment.empty()
                .putBoolean("signed", true)
                .putString("signer", params.player.uuid().toString())
                .putString("signer_name", params.player.name()));
        return ItemModificationResult.success("You have signed this item! It can no longer be edited.");
    }

    private static ItemModificationResult unsign(ItemStack stack, Player player) {
        if (!isPlayerSigner(player, stack)) return ItemModificationResult.WRONG_SIGNER;
        stack.removeAttachmentKeys("signed", "signer", "signer_name");

        List<Message> lines = new ArrayList<>(stack.lore());
        if (!lines.isEmpty()) {
            Message last = lines.get(lines.size() - 1);
            if (TextUtil.UNIFORM_FONT.equals(last.font())) {
                lines.remove(lines.size() - 1);
                stack.setLore(lines);
            }
        }

        return ItemModificationResult.success("The item has been unsigned and is now editable again.");
    }

    private static boolean isPlayerSigner(Player player, ItemStack stack) {
        Attachment data = stack.attachment();
        if (!data.has("signed")) return false;
        return data.getString("signer", "").equals(player.uuid().toString());
    }
}
