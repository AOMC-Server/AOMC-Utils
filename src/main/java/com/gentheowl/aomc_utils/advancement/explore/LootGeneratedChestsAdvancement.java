package com.gentheowl.aomc_utils.advancement.explore;


import com.gentheowl.aomc_utils.advancement.core.persistent.PlayerCounters;
import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class LootGeneratedChestsAdvancement extends SimpleAdvancement {
    private static final int TARGET = 200;

    private final AdvancementHolder advancement;

    public LootGeneratedChestsAdvancement(MinecraftServer server) {
        this.advancement = server.getAdvancements().get(ModAdvancements.LOOT_GENERATED_CHESTS_ID);
    }

    @Override
    protected AdvancementHolder advancement() {
        return advancement;
    }

    @Override
    public void register() {
        // mixin-driven
    }

    public void onGeneratedLoot(ServerPlayer player) {
        if (advancement() == null) return;
        if (!hasParent(player) || hasThis(player)) return;

        int now = increment(player, PlayerCounters.CounterKey.GENERATED_LOOT_CHESTS);

        if (now >= TARGET) {
            complete(player);
        }
    }
}
