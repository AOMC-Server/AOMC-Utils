package com.gentheowl.aomc_utils.advancement.core;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class SimpleAdvancement {
    protected abstract Identifier advancementId();
    public abstract void register();
    protected String criterion() {
        return "impossible";
    }

    protected final int get(ServerPlayer player, PlayerCounters.CounterKey key) {
        return PlayerCounters.get(player).get(key);
    }

    protected final int set(ServerPlayer player, PlayerCounters.CounterKey key, int value) {
        PlayerCounters.get(player).set(key, value);
        return value;
    }

    protected final int increment(ServerPlayer player, PlayerCounters.CounterKey key) {
        return PlayerCounters.get(player).increment(key);
    }

    protected final int increment(ServerPlayer player, PlayerCounters.CounterKey key, int amount) {
        if (amount <= 0) return get(player, key);

        var data = PlayerCounters.get(player);
        int before = data.get(key);
        int after = before + amount;
        data.set(key, after);
        return after;
    }

    /* ---------- COMPLETION ---------- */

    protected final void complete(ServerPlayer player) {
        AdvancementHolder holder =
                player.server.getAdvancements().get(advancementId());

        if (holder != null) {
            player.getAdvancements().award(holder, criterion());
        }
    }
}