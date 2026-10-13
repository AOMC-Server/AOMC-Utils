package com.gentheowl.aomc_utils.progression;

import parliament.api.Parliament;
import parliament.api.advancement.AdvancementDefinition;
import parliament.api.advancement.AdvancementType;
import parliament.api.text.Color;
import parliament.api.text.Message;

import static com.gentheowl.aomc_utils.progression.ProgressionIds.*;

final class ProgressionTree {

    private ProgressionTree() {
    }

    static void define(Parliament parliament) {
        var advancements = parliament.advancements();

        advancements.define(AdvancementDefinition.root(KILL_STUART)
                .icon("minecraft:wandering_trader_spawn_egg")
                .title(Message.literal("In Cold Blood"))
                .description(Message.literal("Start your journey to the top..."))
                .background("minecraft:entity/end_portal")
                // Progress saved before the Parliament port uses this criterion name.
                .criterionName("killed_stuart"));

        advancements.define(AdvancementDefinition.child(ALL_EFFECTS, KILL_STUART)
                .icon("minecraft:totem_of_undying")
                .title(Message.literal("How did we get here? (2)"))
                .description(Message.literal("You know what to do."))
                .type(AdvancementType.CHALLENGE)
                .criterionName("all_effects"));

        // Combat branch
        challenge(parliament, COMBAT_START, ALL_EFFECTS, "minecraft:netherite_sword",
                "Path of Combat", "Prove your mastery of battle by slaying one of each boss.");
        challenge(parliament, KILL_1000_ZOMBIES, COMBAT_START, "minecraft:zombie_head",
                "Overworld Crusader", "Kill 1000 zombies.");
        challenge(parliament, KILL_1000_PIGLINS, KILL_1000_ZOMBIES, "minecraft:piglin_head",
                "Nether Crusader", "Kill 1000 piglins.");
        challenge(parliament, KILL_1000_ENDERMEN, KILL_1000_PIGLINS, "minecraft:ender_pearl",
                "End Crusader", "Kill 1000 endermen.");
        goal(parliament, KILL_MANY_MOBS, KILL_1000_ENDERMEN, "minecraft:netherite_axe",
                "Master of Combat", "Kill 100,000 mobs.");

        // Exploration branch
        challenge(parliament, LOOT_GENERATED_CHESTS, ALL_EFFECTS, "minecraft:chest",
                "Path of Exploration", "Prove your mastery by looting 200 naturally-generated chests.");
        challenge(parliament, LOOT_TRIAL_VAULTS, LOOT_GENERATED_CHESTS, "minecraft:trial_key",
                "Vault Raider", "Loot 100 trial vaults.");
        challenge(parliament, WALK_DISTANCE, LOOT_TRIAL_VAULTS, "minecraft:leather_boots",
                "Walk in the Park", "Walk 100 km.");
        challenge(parliament, FLY_DISTANCE, WALK_DISTANCE, "minecraft:elytra",
                "Early Departure", "Fly 1000 km with elytra.");
        goal(parliament, VISIT_STRONGHOLDS, FLY_DISTANCE, "minecraft:ender_eye",
                "Master of Exploration", "Visit 64 unique strongholds.");

        // Wealth branch
        challenge(parliament, SHULKER_COBBLE, ALL_EFFECTS, "minecraft:cobblestone",
                "Path of Wealth", "Observe a shulker filled entirely with cobblestone.");
        challenge(parliament, SHULKER_IRON, SHULKER_COBBLE, "minecraft:iron_ingot",
                "A Flower for You", "Observe a shulker filled entirely with iron ingot.");
        challenge(parliament, SHULKER_DIAMONDS, SHULKER_IRON, "minecraft:diamond",
                "Blood Diamonds", "Observe a shulker filled entirely with diamonds.");
        challenge(parliament, SHULKER_DEBRIS, SHULKER_DIAMONDS, "minecraft:ancient_debris",
                "Adamant Hoard", "Observe a shulker filled entirely with ancient debris.");
        goal(parliament, SHULKER_STAR, SHULKER_DEBRIS, "minecraft:nether_star",
                "Master of Wealth", "Observe a shulker filled entirely with nether star.");

        // Peak
        advancements.define(AdvancementDefinition.child(OLYMPUS_GATE, SHULKER_STAR)
                .icon("minecraft:beacon")
                .title(Message.literal("One More Step"))
                .description(Message.literal("End your journey to the top..."))
                .type(AdvancementType.GOAL)
                .announceToChat(false)
                .hidden(true));

        advancements.define(AdvancementDefinition.child(BMPOAT, OLYMPUS_GATE)
                .icon("minecraft:end_portal_frame")
                .title(Message.literal("BMPOAT").color(Color.RED))
                .description(Message.literal("Construct the final altar."))
                .type(AdvancementType.GOAL));
    }

    private static void challenge(Parliament parliament, String id, String parentId,
                                  String icon, String title, String description) {
        parliament.advancements().define(AdvancementDefinition.child(id, parentId)
                .icon(icon)
                .title(Message.literal(title))
                .description(Message.literal(description))
                .type(AdvancementType.CHALLENGE));
    }

    private static void goal(Parliament parliament, String id, String parentId,
                             String icon, String title, String description) {
        parliament.advancements().define(AdvancementDefinition.child(id, parentId)
                .icon(icon)
                .title(Message.literal(title))
                .description(Message.literal(description))
                .type(AdvancementType.GOAL));
    }
}
