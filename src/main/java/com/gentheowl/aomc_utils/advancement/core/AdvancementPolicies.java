package com.gentheowl.aomc_utils.advancement.core;

import com.gentheowl.aomc_utils.advancement.combat.Kill1000EndermenAdvancement;
import com.gentheowl.aomc_utils.advancement.combat.Kill1000PiglinsAdvancement;
import com.gentheowl.aomc_utils.advancement.combat.Kill1000ZombiesAdvancement;
import com.gentheowl.aomc_utils.advancement.combat.KillBossAdvancement;
import com.gentheowl.aomc_utils.advancement.explore.*;
import com.gentheowl.aomc_utils.advancement.peak.BMPOATAdvancement;
import com.gentheowl.aomc_utils.advancement.wealth.*;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public final class AdvancementPolicies {
    private static final Map<Class<? extends SimpleAdvancement>, SimpleAdvancement> REGISTRY = new HashMap<>();

    private AdvancementPolicies() {}

    public static void register(MinecraftServer server) {
        REGISTRY.clear();

        put(new KillBossAdvancement(server));
        put(new Kill1000ZombiesAdvancement(server));
        put(new Kill1000PiglinsAdvancement(server));
        put(new Kill1000EndermenAdvancement(server));
        put(new LootGeneratedChestsAdvancement(server));
        put(new LootTrialVaultsAdvancement(server));
        put(new WalkDistanceAdvancement(server));
        put(new FlyDistanceAdvancement(server));
        put(new VisitStrongholdsAdvancement(server));
        put(new ShulkerOfCobbleAdvancement(server));
        put(new ShulkerOfIronAdvancement(server));
        put(new ShulkerOfDiamondsAdvancement(server));
        put(new ShulkerOfDebrisAdvancement(server));
        put(new ShulkerOfStarAdvancement(server));

        put(new BMPOATAdvancement(server));

        for (SimpleAdvancement adv : REGISTRY.values()) {
            adv.register();
        }
    }

    private static void put(SimpleAdvancement adv) {
        REGISTRY.put(adv.getClass().asSubclass(SimpleAdvancement.class), adv);
    }

    @SuppressWarnings("unchecked")
    public static <T extends SimpleAdvancement> T get(Class<T> type) {
        SimpleAdvancement adv = REGISTRY.get(type);
        if (adv == null) {
            throw new IllegalStateException("Advancement not registered: " + type.getSimpleName());
        }
        return (T) adv;
    }
}
