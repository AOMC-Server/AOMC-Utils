package com.gentheowl.aomc_utils.advancement;


import com.gentheowl.aomc_utils.advancement.core.PlayerCounters;
import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

public final class Kill1000PiglinsAdvancement extends SimpleAdvancement {
    private static final int TARGET = 1000;
    private final AdvancementHolder advancement;

    public Kill1000PiglinsAdvancement(MinecraftServer server) {
        this.advancement = server.getAdvancements().get(ModAdvancements.KILL_1000_PIGLINS_ID);
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

            // piglins (not zombified)
            EntityType<?> t = killed.getType();
            if (t != EntityType.PIGLIN && t != EntityType.PIGLIN_BRUTE) return;

            int now = increment(player, PlayerCounters.CounterKey.PIGLIN_KILLS);

            if (now >= TARGET) {
                complete(player);
            }
        });
    }
}