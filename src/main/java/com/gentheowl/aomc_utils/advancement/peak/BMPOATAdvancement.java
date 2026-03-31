package com.gentheowl.aomc_utils.advancement.peak;

import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;

public final class OlympusGateAdvancement extends SimpleAdvancement {
    private final MinecraftServer server;
    private final AdvancementHolder gate;

    public OlympusGateAdvancement(MinecraftServer server) {
        this.server = server;
        this.gate = server.getAdvancements().get(ModAdvancements.OLYMPUS_GATE_ID);
    }

    @Override
    protected AdvancementHolder advancement() {
        return gate;
    }

    @Override
    public void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, srv) -> tryUnlock(handler.getPlayer()));
    }

    public void tryUnlock(ServerPlayer player) {
        if (advancement() == null) return;
        if (hasThis(player)) return;

        if (isDone(player, ModAdvancements.KILL_MANY_MOBS_ID)
                && isDone(player, ModAdvancements.VISIT_STRONGHOLDS_ID)
                && isDone(player, ModAdvancements.SHULKER_STAR_ID)) {
            complete(player);
        }
    }

    private boolean isDone(ServerPlayer player, net.minecraft.resources.Identifier id) {
        AdvancementHolder h = server.getAdvancements().get(id);
        return h != null && player.getAdvancements().getOrStartProgress(h).isDone();
    }
}