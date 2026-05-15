package com.gentheowl.aomc_utils.advancement.peak;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class DragonSpawnEggSequence {

    private static final List<DragonSpawnEggSequence> ACTIVE = new ArrayList<>();
    private static boolean TICK_HOOKED = false;
    private static int lastServerTick = -1;

    private static final int BUILDUP_TICKS = 120; // 6 seconds
    private static final int LIGHTNING_START = 30;
    private static final int LIGHTNING_END = 120;

    private final ServerLevel level;
    private final BlockPos beaconPos;
    private final RandomSource rng;
    private int tick = 0;

    private DragonSpawnEggSequence(ServerLevel level, BlockPos beaconPos) {
        this.level = level;
        this.beaconPos = beaconPos.immutable();
        this.rng = level.random;
    }

    public static void start(ServerLevel level, BlockPos beaconPos) {
        hookOnce();
        ACTIVE.add(new DragonSpawnEggSequence(level, beaconPos));
    }

    private static void hookOnce() {
        if (TICK_HOOKED) return;
        TICK_HOOKED = true;
        ServerTickEvents.END_SERVER_TICK.register(DragonSpawnEggSequence::tickAll);
    }

    private static void tickAll(MinecraftServer server) {
        // hard guard against duplicate registrations
        int now = server.getTickCount();
        if (now == lastServerTick) return;
        lastServerTick = now;

        if (ACTIVE.isEmpty()) return;
        ACTIVE.removeIf(seq -> !seq.step(server));
    }

    private boolean step(MinecraftServer server) {
        if (tick == 0) broadcastDragonRoar(server);

        float t = Math.min(1f, tick / (float) BUILDUP_TICKS);

        spawnInwardParticles(t);

        if (tick >= LIGHTNING_START && tick <= LIGHTNING_END) {
            strikeLightningNearBeacon();
        }

        tick++;

        if (tick >= BUILDUP_TICKS) {
            placeDragonEgg();
            finalBurst();
            return false;
        }

        return true;
    }

    private void broadcastDragonRoar(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.level().playSound(
                    null,
                    p.blockPosition(),
                    SoundEvents.ENDER_DRAGON_DEATH,
                    SoundSource.MASTER,
                    1.0f,
                    1.0f
            );
        }
    }

    private void spawnInwardParticles(float t) {
        double cx = beaconPos.getX() + 0.5;
        double cy = beaconPos.getY() + 0.9;
        double cz = beaconPos.getZ() + 0.5;

        // start wide, end tight
        double radius = 5.0 * (1.0 - t) + 0.4;

        // aggressive speed ramp so motion is obvious
        double speed = 0.06 + 0.30 * (t * t); // 0.06 -> 0.36

        // more particles later
        int count = Math.min(140, 10 + (int) (t * t * 110));

        for (int i = 0; i < count; i++) {
            // spawn on a ring + a bit of height variance
            double ang = (tick * 0.25) + (i * (Math.PI * 2.0 / count));
            double x = cx + Math.cos(ang) * radius;
            double z = cz + Math.sin(ang) * radius;
            double y = cy + (rng.nextDouble() - 0.5) * (1.6 * (1.0 - t) + 0.1);

            // velocity pointing to center
            double dx = cx - x;
            double dy = cy - y;
            double dz = cz - z;

            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 1e-6) len = 1.0;

            dx = (dx / len) * speed;
            dy = (dy / len) * speed;
            dz = (dz / len) * speed;

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    x, y, z,
                    1,
                    dx, dy, dz,
                    0.0
            );
        }
    }

    private void strikeLightningNearBeacon() {

        double x = beaconPos.getX() + 0.5;
        double y = beaconPos.getY() + 1.0;
        double z = beaconPos.getZ() + 0.5;

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt == null) return;

        bolt.setPos(new Vec3(x, y, z));
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
    }

    private void placeDragonEgg() {
        BlockPos eggPos = beaconPos.above();
        if (!level.getBlockState(eggPos).canBeReplaced()) return;
        level.setBlockAndUpdate(eggPos, Blocks.DRAGON_EGG.defaultBlockState());
    }

    private void finalBurst() {
        double cx = beaconPos.getX() + 0.5;
        double cy = beaconPos.getY() + 1.2;
        double cz = beaconPos.getZ() + 0.5;

        level.sendParticles(
                ParticleTypes.DUST_PLUME,
                cx, cy, cz,
                80,
                0.8, 0.5, 0.8,
                0.02
        );
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy, cz, 10, 0.3, 0.3, 0.3, 0);
        level.sendParticles(ParticleTypes.POOF, cx, cy, cz, 40, 1.5, 0.8, 1.5, 0.02);
        var breath = PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 5.0f); // power: tweak 0..?
        level.sendParticles(
                breath,
                cx, cy, cz,
                50,          // count
                0.8, 0.4, 0.8, // spread dx/dy/dz
                0.02         // speed
        );
    }
}
