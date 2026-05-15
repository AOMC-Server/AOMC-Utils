package com.gentheowl.aomc_utils.advancement.explore;

import com.gentheowl.aomc_utils.advancement.core.AdvancementPolicies;
import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import com.gentheowl.aomc_utils.advancement.core.persistent.PlayerCounters;
import com.gentheowl.aomc_utils.advancement.core.persistent.StrongholdVisits;
import com.gentheowl.aomc_utils.advancement.peak.BMPOATAdvancement;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Map;

public class VisitStrongholdsAdvancement extends SimpleAdvancement {
    private static final int TARGET = 64;
    private static final int CHECK_EVERY = 20; // ticks, to not lag the server
    private final AdvancementHolder advancement;

    public VisitStrongholdsAdvancement(MinecraftServer server) {
        this.advancement = server.getAdvancements().get(ModAdvancements.VISIT_STRONGHOLDS_ID);
    }

    @Override
    protected AdvancementHolder advancement() {
        return advancement;
    }

    @Override
    public void register() {
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server) {
        int tick = server.getTickCount();
        if (tick % CHECK_EVERY != 0 ) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (advancement == null) continue;
            if (!hasParent(player) || hasThis(player)) return;
            ServerLevel level = player.level();

            long key = strongholdKeyAt(level, player.blockPosition());
            StrongholdVisits visits = PlayerCounters.strongholds(player);

            if (key == Long.MIN_VALUE) {
                visits.clearLast();
                continue;
            }

            if(!visits.recordIfNew(key)) continue;

            int now = visits.count();

            if (now >= TARGET) {
                complete(player);
                AdvancementPolicies.get(BMPOATAdvancement.class).onBranchEnd(player);
            }
        }
    }

    private static final Identifier STRONGHOLD_ID = Identifier.withDefaultNamespace("stronghold"); // i think

    public static long strongholdKeyAt(ServerLevel level, BlockPos pos) {
        Map<Structure, LongSet> loc = level.structureManager().getAllStructuresAt(pos);
        if (loc.isEmpty()) return Long.MIN_VALUE;

        Registry<Structure> structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        for (Map.Entry<Structure, LongSet> entry : loc.entrySet()) {
            Identifier id = structures.getKey(entry.getKey());
            if (id == null || !id.equals(STRONGHOLD_ID)) continue;

            LongSet start = entry.getValue();
            if (start == null) continue;

            LongIterator it = start.iterator();
            return it.hasNext() ? it.nextLong() : Long.MIN_VALUE;
        }

        return Long.MIN_VALUE;
    }
}
