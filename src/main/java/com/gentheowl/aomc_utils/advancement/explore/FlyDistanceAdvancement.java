package com.gentheowl.aomc_utils.advancement.explore;


import com.gentheowl.aomc_utils.advancement.core.persistent.PlayerCounters;
import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class FlyDistanceAdvancement extends SimpleAdvancement {
    private static final int TARGET_CM = 100_000_000; // 1000 km
    private final AdvancementHolder advancement;

    public FlyDistanceAdvancement(MinecraftServer server) {
        this.advancement = server.getAdvancements().get(ModAdvancements.FLY_DISTANCE_ID);
    }

    @Override
    protected AdvancementHolder advancement() {
        return advancement;
    }

    @Override
    public void register() {
        // stat-hook driven
    }

    public void onAviateCm(ServerPlayer player, int cm) {
        if (cm <= 0) return;
        if (advancement() == null) return;
        if (!hasParent(player) || hasThis(player)) return;

        int before = get(player, PlayerCounters.CounterKey.ELYTRA_CM);
        int now = before + cm;
        set(player, PlayerCounters.CounterKey.ELYTRA_CM, now);

        if (now >= TARGET_CM) {
            complete(player);
        }
    }
}