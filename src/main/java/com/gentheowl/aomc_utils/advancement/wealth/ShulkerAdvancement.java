package com.gentheowl.aomc_utils.advancement.wealth;

import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class ShulkerAdvancement extends SimpleAdvancement {
    private final AdvancementHolder holder;

    protected ShulkerAdvancement(MinecraftServer server) {
        this.holder = server.getAdvancements().get(advId());
    }

    protected abstract Identifier advId();
    protected abstract Item targetItem();

    @Override
    protected final AdvancementHolder advancement() {
        return holder;
    }

    @Override
    public void register() {
        // mixin-driven via onMenuOpened(...)
    }

    public void onMenuOpened(ServerPlayer player, AbstractContainerMenu menu) {
        if (advancement() == null) return;
        if (!(menu instanceof ShulkerBoxMenu shulker)) return;
        if (!hasParent(player) || hasThis(player)) return;

        if (isFullOf(shulker, targetItem())) {
            complete(player);
            afterCompleted(player);
        }
    }

    private static boolean isFullOf(ShulkerBoxMenu menu, Item item) {
        int perStack = item.getDefaultMaxStackSize();
        if (perStack <= 0) return false;

        for (int i = 0; i < 27; i++) {
            ItemStack s = menu.getSlot(i).getItem();
            if (s.isEmpty() || !s.is(item) || s.getCount() != perStack) return false;
        }
        return true;
    }

    protected void afterCompleted(ServerPlayer player) {}
}
