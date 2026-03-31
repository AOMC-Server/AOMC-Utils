package com.gentheowl.aomc_utils.advancement.core;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

@SuppressWarnings("UnstableApiUsage")
public final class PlayerCounters {
    public enum CounterKey {
        DRAGON_KILLS("dragon_kills"),
        WARDEN_KILLS("warden_kills"),
        WITHER_KILLS("wither_kills"),
        ELDER_GUARDIAN_KILLS("elder_guardian_kills"),
        ZOMBIE_KILLS("zombie_kills"),
        PIGLIN_KILLS("piglin_kills"),
        ENDERMAN_KILLS("enderman_kills"),
        MOB_KILLS("mob_kills"),
        GENERATED_LOOT_CHESTS("generated_loot_chests"),
        TRIAL_VAULT_LOOTS("trial_vault_loots"),
        WALK_CM("walk_cm"),
        ELYTRA_CM("elytra_cm");

        private final AttachmentType<Integer> type;

        CounterKey(String path) {
            this.type = makeCounter(path);
        }

        public AttachmentType<Integer> type() {
            return type;
        }
    }

    public static CounterData get(AttachmentTarget target) {
        return new CounterData(target);
    }

    public record CounterData(AttachmentTarget target) {
        public int get(CounterKey key) {
            return target.getAttachedOrElse(key.type(), 0);
        }

        public void set(CounterKey key, int value) {
            target.setAttached(key.type(), value);
        }

        public int increment(CounterKey key) {
            AttachmentType<Integer> type = key.type();

            Integer v = target.modifyAttached(type, n -> (n == null ? 0 : n) + 1);
            if (v == null) {
                target.setAttached(type, 1);
                return 1;
            }
            return v;
        }
    }

    private static AttachmentType<Integer> makeCounter(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(AOMCUtils.MOD_ID, path);
        return AttachmentRegistry.create(id, builder -> builder
                .initializer(() -> 0)
                .syncWith(ByteBufCodecs.INT, AttachmentSyncPredicate.all())
                .persistent(Codec.INT)
                .copyOnDeath()
        );
    }

    private PlayerCounters() {}
}