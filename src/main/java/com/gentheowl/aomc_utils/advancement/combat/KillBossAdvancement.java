package com.gentheowl.aomc_utils.advancement.combat;

import com.gentheowl.aomc_utils.advancement.core.persistent.PlayerCounters;
import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import java.util.Map;

public final class KillBossAdvancement extends SimpleAdvancement {
    private final AdvancementHolder advancement;
    private static final Map<EntityType<?>, PlayerCounters.CounterKey> BOSS_COUNTERS =
            Map.of(
                    EntityTypes.ENDER_DRAGON, PlayerCounters.CounterKey.DRAGON_KILLS,
                    EntityTypes.WARDEN, PlayerCounters.CounterKey.WARDEN_KILLS,
                    EntityTypes.WITHER, PlayerCounters.CounterKey.WITHER_KILLS,
                    EntityTypes.ELDER_GUARDIAN, PlayerCounters.CounterKey.ELDER_GUARDIAN_KILLS
            );

    public KillBossAdvancement(MinecraftServer server) {
        this.advancement = server.getAdvancements().get(ModAdvancements.COMBAT_START_ID);
    }

    @Override
    protected AdvancementHolder advancement() {
        return advancement;
    }

    @Override
    public void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((killed, damageSource) -> {
            if (!(killed.getKillCredit() instanceof ServerPlayer player)) return;
            if (!hasParent(player) || hasThis(player)) return;
            PlayerCounters.CounterKey key = BOSS_COUNTERS.get(killed.getType());
            if (key == null) return;
            if (!incrementOnce(player, key)) return;

            if (get(player, PlayerCounters.CounterKey.DRAGON_KILLS) > 0
                    && get(player, PlayerCounters.CounterKey.WARDEN_KILLS) > 0
                    && get(player, PlayerCounters.CounterKey.WITHER_KILLS) > 0
                    && get(player, PlayerCounters.CounterKey.ELDER_GUARDIAN_KILLS) > 0) {
                complete(player);
            }
        });
    }
}
