package com.gentheowl.aomc_utils.advancement.wealth;


import com.gentheowl.aomc_utils.advancement.core.AdvancementPolicies;
import com.gentheowl.aomc_utils.advancement.peak.BMPOATAdvancement;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ShulkerOfStarAdvancement extends ShulkerAdvancement {
    public ShulkerOfStarAdvancement(MinecraftServer server) {
        super(server);
    }

    @Override
    protected Identifier advId() {
        return ModAdvancements.SHULKER_STAR_ID;
    }

    @Override
    protected Item targetItem() {
        return Items.NETHER_STAR;
    }

    @Override
    protected void afterCompleted(ServerPlayer player) {
        AdvancementPolicies.get(BMPOATAdvancement.class).onBranchEnd(player);
    }
}