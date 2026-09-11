package com.gentheowl.aomc_utils.advancement.combat;

import com.gentheowl.aomc_utils.advancement.core.AdvancementPolicies;
import com.gentheowl.aomc_utils.advancement.core.persistent.PlayerCounters;
import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import com.gentheowl.aomc_utils.advancement.peak.BMPOATAdvancement;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

public final class KillManyMobsAdvancement extends SimpleAdvancement {
    private static final int TARGET = 100_000;
    private final AdvancementHolder advancement;

    public KillManyMobsAdvancement(MinecraftServer server) {
        this.advancement = server.getAdvancements().get(ModAdvancements.KILL_MANY_MOBS_ID);
    }

    @Override
    protected AdvancementHolder advancement() {
        return advancement;
    }

    @Override
    public void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((killed, damageSource) -> {
            if (!(killed instanceof Mob)) return;
            if (!(killed.getKillCredit() instanceof ServerPlayer player)) return;
            if (!hasParent(player) || hasThis(player)) return;

            int now = increment(player, PlayerCounters.CounterKey.MOB_KILLS);

            if (now >= TARGET) {
                complete(player);
                AdvancementPolicies.get(BMPOATAdvancement.class).onBranchEnd(player);
            }
        });
    }
}