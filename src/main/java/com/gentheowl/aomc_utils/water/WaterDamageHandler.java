package com.gentheowl.aomc_utils.water;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;

public class WaterDamageHandler {
    private static final int CHECK_EVERY = 10; // ticks (1 second)
    private static final float DAMAGE = 2.0f;  // 1 heart

    public static void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % CHECK_EVERY != 0) return;

        if (player.isInWater() || isInRain(player)) {
            ServerLevel level = player.level();
            player.hurtServer(level, level.damageSources().source(DamageTypes.DROWN), DAMAGE);
        }
    }

    private static boolean isInRain(ServerPlayer player) {
        ServerLevel level = player.level();
        return level.isRaining() && level.isRainingAt(player.blockPosition().above());
    }
}
