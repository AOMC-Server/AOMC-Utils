package com.gentheowl.aomc_utils.progression;

import parliament.api.Parliament;
import parliament.api.ParliamentMod;
import parliament.api.data.Counter;
import parliament.api.data.ValueSet;
import parliament.api.entity.Player;
import parliament.api.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gentheowl.aomc_utils.progression.ProgressionIds.*;


public final class AomcProgression implements ParliamentMod {

    private static final int KILL_TARGET = 1000;
    private static final int MOB_KILL_TARGET = 100_000;
    private static final int CHEST_TARGET = 200;
    private static final int VAULT_TARGET = 100;
    private static final int WALK_TARGET_CM = 10_000_000;   // 100 km
    private static final int FLY_TARGET_CM = 100_000_000;   // 1000 km
    private static final int SHULKER_SLOTS = 27;
    private static final int STRONGHOLD_TARGET = 64;
    private static final int STRONGHOLD_CHECK_EVERY = 20; // ticks, to not lag the server

    private static final Set<String> ZOMBIE_TYPES =
            Set.of("minecraft:zombie", "minecraft:husk", "minecraft:drowned");
    private static final Set<String> PIGLIN_TYPES =
            Set.of("minecraft:piglin", "minecraft:piglin_brute");

    /** Shulker chain: advancement id -> (parent id, required item id). */
    private static final List<String[]> SHULKER_CHAIN = List.of(
            new String[]{SHULKER_COBBLE, ALL_EFFECTS, "minecraft:cobblestone"},
            new String[]{SHULKER_IRON, SHULKER_COBBLE, "minecraft:iron_ingot"},
            new String[]{SHULKER_DIAMONDS, SHULKER_IRON, "minecraft:diamond"},
            new String[]{SHULKER_DEBRIS, SHULKER_DIAMONDS, "minecraft:ancient_debris"},
            new String[]{SHULKER_STAR, SHULKER_DEBRIS, "minecraft:nether_star"});

    private Parliament parliament;
    private OlympusPeak peak;
    private ValueSet strongholdVisits;
    private int strongholdTick;
    private Map<String, Counter> bossCounters;
    private Counter zombieKills;
    private Counter piglinKills;
    private Counter endermanKills;
    private Counter mobKills;
    private Counter lootChests;
    private Counter vaultLoots;
    private Counter walkCm;
    private Counter elytraCm;

    @Override
    public void initialize(Parliament parliament) {
        this.parliament = parliament;

        bossCounters = Map.of(
                "minecraft:ender_dragon", parliament.playerData().counter(DRAGON_KILLS),
                "minecraft:warden", parliament.playerData().counter(WARDEN_KILLS),
                "minecraft:wither", parliament.playerData().counter(WITHER_KILLS),
                "minecraft:elder_guardian", parliament.playerData().counter(ELDER_GUARDIAN_KILLS));
        zombieKills = parliament.playerData().counter(ZOMBIE_KILLS);
        piglinKills = parliament.playerData().counter(PIGLIN_KILLS);
        endermanKills = parliament.playerData().counter(ENDERMAN_KILLS);
        mobKills = parliament.playerData().counter(MOB_KILLS);
        lootChests = parliament.playerData().counter(GENERATED_LOOT_CHESTS);
        vaultLoots = parliament.playerData().counter(TRIAL_VAULT_LOOTS);
        walkCm = parliament.playerData().counter(WALK_CM);
        elytraCm = parliament.playerData().counter(ELYTRA_CM);

        strongholdVisits = parliament.playerData().valueSet("aomc_utils:stronghold_visit_set");

        ProgressionTree.define(parliament);

        peak = new OlympusPeak(parliament);
        peak.register();

        parliament.events().onEntityDeath(this::onEntityDeath);
        parliament.events().onStatIncrement(event -> {
            switch (event.statId()) {
                case "minecraft:walk_one_cm" -> onDistance(event.player(), walkCm,
                        event.amount(), WALK_DISTANCE, LOOT_TRIAL_VAULTS, WALK_TARGET_CM, false);
                case "minecraft:aviate_one_cm" -> onDistance(event.player(), elytraCm,
                        event.amount(), FLY_DISTANCE, WALK_DISTANCE, FLY_TARGET_CM, false);
            }
        });
        parliament.events().onLootGenerated(event -> onCounted(event.player(), lootChests,
                LOOT_GENERATED_CHESTS, ALL_EFFECTS, CHEST_TARGET, false));
        parliament.events().onVaultReward(event -> onCounted(event.player(), vaultLoots,
                LOOT_TRIAL_VAULTS, LOOT_GENERATED_CHESTS, VAULT_TARGET, false));
        parliament.events().onMenuOpen(event -> {
            if (event.menuTypeId().equals("minecraft:shulker_box")) {
                onShulkerOpened(event.player(), event.containerItems());
            }
        });
        parliament.events().onServerTick(server -> {
            if (++strongholdTick % STRONGHOLD_CHECK_EVERY != 0) {
                return;
            }
            for (Player player : server.players()) {
                onStrongholdPoll(player);
            }
        });

        // HDWGH2 mirrors vanilla's all-effects advancement: granted the moment
        // the vanilla one is awarded, backfilled on join for anyone who beat
        // the vanilla one earlier.
        parliament.events().onAdvancementAward(event -> {
            if (event.advancementId().equals(VANILLA_ALL_EFFECTS) && !event.isCancelled()) {
                parliament.advancements().grant(event.player(), ALL_EFFECTS);
            }
        });
        parliament.events().onPlayerJoin(player -> {
            if (parliament.advancements().isDone(player, VANILLA_ALL_EFFECTS)) {
                parliament.advancements().grant(player, ALL_EFFECTS);
            }
        });

        ProgressCommand.register(parliament, this);
    }

    private void onEntityDeath(parliament.api.event.EntityDeathEvent event) {
        Player killer = event.killer().orElse(null);
        if (killer == null) {
            return;
        }
        String type = event.entityTypeId();

        if (type.equals("minecraft:wandering_trader")) {
            parliament.advancements().grant(killer, KILL_STUART);
        }

        Counter bossCounter = bossCounters.get(type);
        if (bossCounter != null && gateOpen(killer, COMBAT_START, ALL_EFFECTS)
                && bossCounter.get(killer) == 0) {
            bossCounter.set(killer, 1);
            boolean allBosses = bossCounters.values().stream()
                    .allMatch(counter -> counter.get(killer) > 0);
            if (allBosses) {
                parliament.advancements().grant(killer, COMBAT_START);
            }
        }

        if (ZOMBIE_TYPES.contains(type)) {
            onCounted(killer, zombieKills, KILL_1000_ZOMBIES, COMBAT_START, KILL_TARGET, false);
        } else if (PIGLIN_TYPES.contains(type)) {
            onCounted(killer, piglinKills, KILL_1000_PIGLINS, KILL_1000_ZOMBIES, KILL_TARGET, false);
        } else if (type.equals("minecraft:enderman")) {
            onCounted(killer, endermanKills, KILL_1000_ENDERMEN, KILL_1000_PIGLINS, KILL_TARGET, false);
        }

        if (event.mob()) {
            onCounted(killer, mobKills, KILL_MANY_MOBS, KILL_1000_ENDERMEN, MOB_KILL_TARGET, true);
        }
    }

    private void onCounted(Player player, Counter counter, String advancementId,
                           String parentId, int target, boolean branchEnd) {
        if (!gateOpen(player, advancementId, parentId)) {
            return;
        }
        if (counter.add(player, 1) >= target) {
            parliament.advancements().grant(player, advancementId);
            if (branchEnd) {
                notifyBranchEnd(player);
            }
        }
    }

    private void onDistance(Player player, Counter counter, int amount, String advancementId,
                            String parentId, int target, boolean branchEnd) {
        if (amount <= 0 || !gateOpen(player, advancementId, parentId)) {
            return;
        }
        if (counter.add(player, amount) >= target) {
            parliament.advancements().grant(player, advancementId);
            if (branchEnd) {
                notifyBranchEnd(player);
            }
        }
    }

    private void onShulkerOpened(Player player, List<ItemStack> items) {
        for (String[] entry : SHULKER_CHAIN) {
            String advancementId = entry[0];
            String parentId = entry[1];
            String itemId = entry[2];
            if (gateOpen(player, advancementId, parentId) && isFullOf(items, itemId)) {
                parliament.advancements().grant(player, advancementId);
                if (advancementId.equals(SHULKER_STAR)) {
                    notifyBranchEnd(player);
                }
            }
        }
    }

    private static boolean isFullOf(List<ItemStack> items, String itemId) {
        if (items.size() != SHULKER_SLOTS) {
            return false;
        }
        for (ItemStack stack : items) {
            if (stack.isEmpty() || !stack.itemId().equals(itemId)
                    || stack.count() != stack.maxCount()) {
                return false;
            }
        }
        return true;
    }

    private void onStrongholdPoll(Player player) {
        if (!gateOpen(player, VISIT_STRONGHOLDS, FLY_DISTANCE)) {
            return;
        }
        player.world().structureInstanceAt(player.blockPosition(), "minecraft:stronghold")
                .ifPresent(key -> {
                    if (strongholdVisits.add(player, key)
                            && strongholdVisits.size(player) >= STRONGHOLD_TARGET) {
                        parliament.advancements().grant(player, VISIT_STRONGHOLDS);
                        notifyBranchEnd(player);
                    }
                });
    }

    int strongholdVisitCount(Player player) {
        return strongholdVisits.size(player);
    }

    /** Open = parent completed, this one not yet. */
    private boolean gateOpen(Player player, String advancementId, String parentId) {
        return !parliament.advancements().isDone(player, advancementId)
                && parliament.advancements().isDone(player, parentId);
    }

    private void notifyBranchEnd(Player player) {
        peak.onBranchEnd(player);
    }

    Counter counterByName(String name) {
        return switch (name) {
            case "dragon_kills" -> bossCounters.get("minecraft:ender_dragon");
            case "warden_kills" -> bossCounters.get("minecraft:warden");
            case "wither_kills" -> bossCounters.get("minecraft:wither");
            case "elder_guardian_kills" -> bossCounters.get("minecraft:elder_guardian");
            case "zombie_kills" -> zombieKills;
            case "piglin_kills" -> piglinKills;
            case "enderman_kills" -> endermanKills;
            case "mob_kills" -> mobKills;
            case "generated_loot_chests" -> lootChests;
            case "trial_vault_loots" -> vaultLoots;
            case "walk_cm" -> walkCm;
            case "elytra_cm" -> elytraCm;
            default -> null;
        };
    }
}
