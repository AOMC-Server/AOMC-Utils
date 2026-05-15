package com.gentheowl.aomc_utils.advancement.core;

import com.gentheowl.aomc_utils.advancement.core.persistent.PlayerCounters;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;

public abstract class SimpleAdvancement {
    /** The AdvancementHolder for this advancement */
    protected abstract AdvancementHolder advancement();

    /** Register ALL Fabric event listeners here */
    public abstract void register();

    /** Criterion name used in JSON (default: "impossible") */
    protected String criterion() {
        return ModAdvancements.IMPOSSIBLE_NAME;
    }

    /* ---------- COUNTER HELPERS ---------- */
    protected final int get(ServerPlayer player, PlayerCounters.CounterKey key) {
        return PlayerCounters.get(player).get(key);
    }

    protected final void set(ServerPlayer player, PlayerCounters.CounterKey key, int value) {
        PlayerCounters.get(player).set(key, value);
    }

    protected final int increment(ServerPlayer player, PlayerCounters.CounterKey key) {
        return PlayerCounters.get(player).increment(key);
    }

    protected boolean hasThis(ServerPlayer player) {
        return player.getAdvancements().getOrStartProgress(advancement()).isDone();
    }

    protected boolean hasParent(ServerPlayer player) {
        AdvancementHolder self = advancement();
        var parentIdOpt = self.value().parent();
        if (parentIdOpt.isEmpty()) {
            return true; // root advancement
        }

        AdvancementHolder parent =
                player.createCommandSourceStack().getServer().getAdvancements().get(parentIdOpt.get());

        if (parent == null) return false;

        return player.getAdvancements()
                .getOrStartProgress(parent)
                .isDone();
    }

    protected final boolean incrementOnce(ServerPlayer player, PlayerCounters.CounterKey key) {
        if (get(player, key) == 0) {
            increment(player, key);
            return true;
        }
        return false;
    }

    /* ---------- COMPLETION ---------- */

    protected final void complete(ServerPlayer player) {
        AdvancementHolder holder = advancement();
        if (holder != null) {
            player.getAdvancements().award(holder, criterion());

        }
    }
}