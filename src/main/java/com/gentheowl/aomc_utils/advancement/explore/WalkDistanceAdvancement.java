package com.gentheowl.aomc_utils.advancement.explore;

import com.gentheowl.aomc_utils.advancement.core.AdvancementPolicies;
import com.gentheowl.aomc_utils.advancement.core.persistent.PlayerCounters;
import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import com.gentheowl.aomc_utils.advancement.peak.BMPOATAdvancement;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class WalkDistanceAdvancement extends SimpleAdvancement {
    private static final int TARGET_CM = 10_000_000; // 100 km

    private final AdvancementHolder advancement;

    public WalkDistanceAdvancement(MinecraftServer server) {
        this.advancement = server.getAdvancements().get(ModAdvancements.WALK_DISTANCE_ID);
    }

    @Override
    protected AdvancementHolder advancement() {
        return advancement;
    }

    @Override
    public void register() {
        // mixin-driven
    }

    public void onWalkCm(ServerPlayer player, int cm) {
        if (cm <= 0) return;
        if (advancement() == null) return;
        if (!hasParent(player) || hasThis(player)) return;

        int before = get(player, PlayerCounters.CounterKey.WALK_CM);
        int now = before + cm;
        set(player, PlayerCounters.CounterKey.WALK_CM, now);

        if (now >= TARGET_CM) {
            complete(player);
        }
    }
}
