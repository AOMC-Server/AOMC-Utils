package com.gentheowl.aomc_utils.advancement.core.persistent;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class StrongholdVisits {
    public static final Codec<StrongholdVisits> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("last", Long.MIN_VALUE).forGetter(v -> v.lastKey),
            Codec.LONG.listOf().optionalFieldOf("Seen", List.of()).forGetter(StrongholdVisits::seenKeysAsList)
    ).apply(instance, (last, seen) -> {
        StrongholdVisits visits = new StrongholdVisits();
        visits.lastKey = last;
        visits.seen.addAll(seen);
        return visits;
    }));

    private long lastKey = Long.MIN_VALUE;
    private final LongSet seen = new LongOpenHashSet();

    public StrongholdVisits() {}

    public int count() {
        return seen.size();
    }

    public boolean recordIfNew(long key) {
        if (key == lastKey) return false;
        lastKey = key;
        return seen.add(key);
    }

    public void clearLast() {
        lastKey = Long.MIN_VALUE;
    }

    // serialization helper
    private List<Long> seenKeysAsList() {
        return seen.longStream().boxed().toList();
    }

    // interface
    @SuppressWarnings("UnstableApiUsage")
    public static AttachmentType<StrongholdVisits> getCounter() {
        return AttachmentRegistry.create(
                Identifier.fromNamespaceAndPath(AOMCUtils.MOD_ID, "stronghold_visits"),
                (AttachmentRegistry.Builder<StrongholdVisits> builder) -> builder
                        .initializer(StrongholdVisits::new)
                        .persistent(CODEC)
                        .copyOnDeath()
        );
    }
}
