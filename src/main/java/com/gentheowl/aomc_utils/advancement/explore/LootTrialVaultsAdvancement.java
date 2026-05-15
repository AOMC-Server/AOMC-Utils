package com.gentheowl.aomc_utils.advancement.explore;
import com.gentheowl.aomc_utils.advancement.core.persistent.PlayerCounters;
import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class LootTrialVaultsAdvancement extends SimpleAdvancement {
    private static final int TARGET = 100;

    private final AdvancementHolder advancement;

    public LootTrialVaultsAdvancement(MinecraftServer server) {
        this.advancement = server.getAdvancements().get(ModAdvancements.LOOT_TRIAL_VAULTS_ID);
    }

    @Override
    protected AdvancementHolder advancement() {
        return advancement;
    }

    @Override
    public void register() {
        // mixin-driven
    }

    public void onTrialVaultLooted(ServerPlayer player) {
        if (advancement() == null) return;
        if (!hasParent(player) || hasThis(player)) return;

        int now = increment(player, PlayerCounters.CounterKey.TRIAL_VAULT_LOOTS);

        if (now >= TARGET) {
            complete(player);
        }
    }
}
