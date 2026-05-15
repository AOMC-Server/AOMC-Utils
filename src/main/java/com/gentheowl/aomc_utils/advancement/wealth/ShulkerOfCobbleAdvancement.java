package com.gentheowl.aomc_utils.advancement.wealth;

import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ShulkerOfCobbleAdvancement extends ShulkerAdvancement {
    public ShulkerOfCobbleAdvancement(MinecraftServer server) {
        super(server);
    }

    @Override
    protected Identifier advId() {
        return ModAdvancements.SHULKER_COBBLE_ID;
    }

    @Override
    protected Item targetItem() {
        return Items.COBBLESTONE;
    }
}