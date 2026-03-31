package com.gentheowl.aomc_utils.advancement;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.advancement.core.PlayerCounters;
import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public final class KillBossAdvancement extends SimpleAdvancement {
    private final AdvancementHolder advancement;
    private static final Map<EntityType<?>, PlayerCounters.CounterKey> BOSS_COUNTERS =
            Map.of(
                    EntityType.ENDER_DRAGON, PlayerCounters.CounterKey.DRAGON_KILLS,
                    EntityType.WARDEN, PlayerCounters.CounterKey.WARDEN_KILLS,
                    EntityType.WITHER, PlayerCounters.CounterKey.WITHER_KILLS,
                    EntityType.ELDER_GUARDIAN, PlayerCounters.CounterKey.ELDER_GUARDIAN_KILLS
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
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, attacker, killed, damageSource) -> {
            if (!(attacker instanceof ServerPlayer player)) return;
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
