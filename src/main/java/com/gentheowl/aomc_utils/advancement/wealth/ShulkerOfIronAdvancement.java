package com.gentheowl.aomc_utils.advancement.wealth;

import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ShulkerOfIronAdvancement extends ShulkerAdvancement {
    public ShulkerOfIronAdvancement(MinecraftServer server) {
        super(server);
    }

    @Override
    protected Identifier advId() {
        return ModAdvancements.SHULKER_IRON_ID;
    }

    @Override
    protected Item targetItem() {
        return Items.IRON_INGOT;
    }
}
