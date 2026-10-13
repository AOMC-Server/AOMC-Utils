package com.gentheowl.aomc_utils.progression;

import parliament.api.Parliament;
import parliament.api.entity.Player;
import parliament.api.world.Position;
import parliament.api.world.Vector;
import parliament.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The dragon-egg spawn ceremony over the olympus beacon: six seconds of
 * inward end-rod particles and lightning, then the egg appears in a burst.
 */
final class DragonEggCeremony {

    private static final int BUILDUP_TICKS = 120; // 6 seconds
    private static final int LIGHTNING_START = 30;
    private static final int LIGHTNING_END = 120;

    private static final List<DragonEggCeremony> ACTIVE = new ArrayList<>();
    private static boolean tickHooked;

    private final Parliament parliament;
    private final World world;
    private final Position beaconPos;
    private final Random random = new Random();
    private int tick;

    private DragonEggCeremony(Parliament parliament, World world, Position beaconPos) {
        this.parliament = parliament;
        this.world = world;
        this.beaconPos = beaconPos;
    }

    static void start(Parliament parliament, World world, Position beaconPos) {
        if (!tickHooked) {
            tickHooked = true;
            parliament.events().onServerTick(server -> ACTIVE.removeIf(c -> !c.step()));
        }
        ACTIVE.add(new DragonEggCeremony(parliament, world, beaconPos));
    }

    private boolean step() {
        if (tick == 0) {
            broadcastDragonRoar();
        }

        float t = Math.min(1f, tick / (float) BUILDUP_TICKS);
        spawnInwardParticles(t);

        if (tick >= LIGHTNING_START && tick <= LIGHTNING_END) {
            world.strikeLightning(new Vector(
                    beaconPos.x() + 0.5, beaconPos.y() + 1.0, beaconPos.z() + 0.5), false);
        }

        tick++;

        if (tick >= BUILDUP_TICKS) {
            placeDragonEgg();
            finalBurst();
            return false;
        }
        return true;
    }

    private void broadcastDragonRoar() {
        for (Player player : parliament.server().players()) {
            player.world().playSound("minecraft:entity.ender_dragon.death",
                    player.blockPosition().center(), 1.0f, 1.0f);
        }
    }

    private void spawnInwardParticles(float t) {
        double cx = beaconPos.x() + 0.5;
        double cy = beaconPos.y() + 0.9;
        double cz = beaconPos.z() + 0.5;

        // start wide, end tight
        double radius = 5.0 * (1.0 - t) + 0.4;
        // aggressive speed ramp so motion is obvious
        double speed = 0.06 + 0.30 * (t * t);
        // more particles later
        int count = Math.min(140, 10 + (int) (t * t * 110));

        for (int i = 0; i < count; i++) {
            double angle = (tick * 0.25) + (i * (Math.PI * 2.0 / count));
            double x = cx + Math.cos(angle) * radius;
            double z = cz + Math.sin(angle) * radius;
            double y = cy + (random.nextDouble() - 0.5) * (1.6 * (1.0 - t) + 0.1);

            double dx = cx - x;
            double dy = cy - y;
            double dz = cz - z;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length < 1e-6) {
                length = 1.0;
            }
            world.spawnParticles("minecraft:end_rod", new Vector(x, y, z), 1,
                    new Vector(dx / length * speed, dy / length * speed, dz / length * speed), 0.0);
        }
    }

    private void placeDragonEgg() {
        Position eggPos = beaconPos.offset(0, 1, 0);
        if (world.blockId(eggPos).equals("minecraft:air")
                || world.blockId(eggPos).equals("minecraft:cave_air")
                || world.blockId(eggPos).equals("minecraft:water")) {
            world.setBlock(eggPos, "minecraft:dragon_egg");
        }
    }

    private void finalBurst() {
        Vector center = new Vector(beaconPos.x() + 0.5, beaconPos.y() + 1.2, beaconPos.z() + 0.5);
        world.spawnParticles("minecraft:dust_plume", center, 80, new Vector(0.8, 0.5, 0.8), 0.02);
        world.spawnParticles("minecraft:explosion_emitter", center, 10, new Vector(0.3, 0.3, 0.3), 0);
        world.spawnParticles("minecraft:poof", center, 40, new Vector(1.5, 0.8, 1.5), 0.02);
        world.spawnParticles("minecraft:dragon_breath", center, 50, new Vector(0.8, 0.4, 0.8), 0.02);
    }
}
