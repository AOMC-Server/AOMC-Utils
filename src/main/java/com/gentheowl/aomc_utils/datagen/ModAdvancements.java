package com.gentheowl.aomc_utils.datagen;

import com.gentheowl.aomc_utils.AOMCUtils;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ModAdvancements {
    private static final Criterion<ImpossibleTrigger.@NotNull TriggerInstance> IMPOSSIBLE_CRITERION = CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());

    public static final String IMPOSSIBLE_NAME = "impossible";
    public static final Identifier KILL_STUART_ID = mod_id("kill_stuart");
    public static final Identifier ALL_EFFECTS_ID = mod_id("all_effects");

    public static final Identifier COMBAT_START_ID = mod_id("combat_start");
    public static final Identifier KILL_1000_ZOMBIES_ID = mod_id("kill_1000_zombies");
    public static final Identifier KILL_1000_PIGLINS_ID = mod_id("kill_1000_piglins");
    public static final Identifier KILL_1000_ENDERMEN_ID = mod_id("kill_1000_endermen");
    public static final Identifier KILL_MANY_MOBS_ID = mod_id("kill_many_mobs");

    public static final Identifier LOOT_GENERATED_CHESTS_ID = mod_id("loot_generated_chests");
    public static final Identifier LOOT_TRIAL_VAULTS_ID = mod_id("loot_trial_vaults");
    public static final Identifier WALK_DISTANCE_ID = mod_id("walk_distance");
    public static final Identifier FLY_DISTANCE_ID = mod_id("fly_distance");
    public static final Identifier VISIT_STRONGHOLDS_ID = mod_id("visit_strongholds");

    public static final Identifier SHULKER_COBBLE_ID = mod_id("shulker_of_cobble");
    public static final Identifier SHULKER_IRON_ID = mod_id("shulker_of_iron");
    public static final Identifier SHULKER_DIAMONDS_ID = mod_id("shulker_of_diamonds");
    public static final Identifier SHULKER_DEBRIS_ID = mod_id("shulker_of_debris");
    public static final Identifier SHULKER_STAR_ID = mod_id("shulker_of_star");

    public static final Identifier OLYMPUS_GATE_ID = mod_id("olympus_gate");
    public static final Identifier BMPOAT_ID = mod_id("bmpoat");

    public static void register(HolderLookup.Provider wrapperLookup, Consumer<AdvancementHolder> consumer) {
       HolderLookup.RegistryLookup<EntityType<?>> entityTypes = wrapperLookup.lookupOrThrow(Registries.ENTITY_TYPE);

        AdvancementHolder KILL_STAURT = Advancement.Builder.advancement()
                .display(
                        Items.WANDERING_TRADER_SPAWN_EGG, // The display icon
                        Component.literal("In Cold Blood"), // The title
                        Component.literal("Start your journey to the top..."), // The description
                        Identifier.withDefaultNamespace("entity/end_portal"), // Background image for the tab in the advancements page, if this is a root advancement (has no parent)
                        AdvancementType.TASK, // TASK, CHALLENGE, or GOAL
                        true, // Show the toast when completing it
                        true, // Announce it to chat
                        false // Hide it in the advancement tab until it's achieved
                )
                .addCriterion("killed_stuart", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(entityTypes, EntityType.WANDERING_TRADER)))
                // Give the advancement an id
                .save(consumer, KILL_STUART_ID.toString());

        AdvancementHolder ALL_EFFECTS = Advancement.Builder.advancement()
                .parent(KILL_STAURT)
                .display(
                        Items.TOTEM_OF_UNDYING,
                        Component.literal("How did we get here? (2)"),
                        Component.literal("You know what to do."),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("all_effects", EffectsChangedTrigger.TriggerInstance.hasEffects(allEffects()))
                .save(consumer, ALL_EFFECTS_ID.toString());

        AdvancementHolder COMBAT_START = challenge(consumer, ALL_EFFECTS, Items.NETHERITE_SWORD,
                "Path of Combat", "Prove your mastery of battle by slaying one of each boss.", COMBAT_START_ID);

        AdvancementHolder KILL_1000_ZOMBIES = challenge(consumer, COMBAT_START, Items.ZOMBIE_HEAD,
                "Overworld Crusader", "Kill 1000 zombies.", KILL_1000_ZOMBIES_ID);

        AdvancementHolder KILL_1000_PIGLINS = challenge(consumer, KILL_1000_ZOMBIES, Items.PIGLIN_HEAD,
                "Nether Crusader", "Kill 1000 piglins.", KILL_1000_PIGLINS_ID);

        AdvancementHolder KILL_1000_ENDERMEN = challenge(consumer, KILL_1000_PIGLINS, Items.ENDER_PEARL,
                "End Crusader", "Kill 1000 endermen.", KILL_1000_ENDERMEN_ID);

        AdvancementHolder KILL_MANY_MOBS = goal(consumer, KILL_1000_ENDERMEN, Items.NETHERITE_AXE,
                "Master of Combat", "Kill 100,000 mobs.", KILL_MANY_MOBS_ID);

        AdvancementHolder LOOT_GENERATED_CHESTS = challenge(consumer, ALL_EFFECTS, Items.CHEST,
                "Path of Exploration", "Prove your mastery by looting 200 naturally-generated chests.", LOOT_GENERATED_CHESTS_ID);

        AdvancementHolder LOOT_TRIAL_VAULTS = challenge(consumer, LOOT_GENERATED_CHESTS, Items.TRIAL_KEY,
                "Vault Raider", "Loot 100 trial vaults.", LOOT_TRIAL_VAULTS_ID);

        AdvancementHolder WALK_DISTANCE = challenge(consumer, LOOT_TRIAL_VAULTS, Items.LEATHER_BOOTS,
                "Walk in the Park", "Walk 100 km.", WALK_DISTANCE_ID);

        AdvancementHolder FLY_DISTANCE = challenge(consumer, WALK_DISTANCE, Items.ELYTRA,
                "Early Departure", "Fly 1000 km with elytra.", FLY_DISTANCE_ID);

        AdvancementHolder VISIT_STRONGHOLD = goal(consumer, FLY_DISTANCE, Items.ENDER_EYE,
                "Master of Exploration", "Visit 64 unique strongholds.", VISIT_STRONGHOLDS_ID);

        AdvancementHolder SHULKER_COBBLE = challenge(consumer, ALL_EFFECTS, Items.COBBLESTONE,
                "Path of Wealth", "Observe a shulker filled entirely with cobblestone.", SHULKER_COBBLE_ID);

        AdvancementHolder SHULKER_IRON = challenge(consumer, SHULKER_COBBLE, Items.IRON_INGOT,
                "A Flower for You", "Observe a shulker filled entirely with iron ingot.", SHULKER_IRON_ID);

        AdvancementHolder SHULKER_DIAMONDS = challenge(consumer, SHULKER_IRON, Items.DIAMOND,
                "Blood Diamonds", "Observe a shulker filled entirely with diamonds.", SHULKER_DIAMONDS_ID);

        AdvancementHolder SHULKER_DEBRIS = challenge(consumer, SHULKER_DIAMONDS, Items.ANCIENT_DEBRIS,
                "Adamant Hoard", "Observe a shulker filled entirely with ancient debris.", SHULKER_DEBRIS_ID);

        AdvancementHolder SHULKER_STAR = goal(consumer, SHULKER_DEBRIS, Items.NETHER_STAR,
                "Master of Wealth", "Observe a shulker filled entirely with nether star.", SHULKER_STAR_ID);

        AdvancementHolder OLYMPUS_GATE = Advancement.Builder.advancement()
                .parent(SHULKER_STAR)
                .display(
                        Items.BEACON, // The display icon
                        Component.literal("One More Step"), // The title
                        Component.literal("End your journey to the top..."), // The description
                        Identifier.withDefaultNamespace("entity/end_portal"), // Background image for the tab in the advancements page, if this is a root advancement (has no parent)
                        AdvancementType.GOAL, // TASK, CHALLENGE, or GOAL
                        true, // Show the toast when completing it
                        false, // Announce it to chat
                        true // Hide it in the advancement tab until it's achieved
                )
                .addCriterion(IMPOSSIBLE_NAME, IMPOSSIBLE_CRITERION)
                // Give the advancement an id
                .save(consumer, OLYMPUS_GATE_ID.toString());

        AdvancementHolder SUMMIT_OLYMPUS = Advancement.Builder.advancement()
                .parent(OLYMPUS_GATE)
                .display(
                        Blocks.END_PORTAL,
                        Component.literal("BMPOAT").withStyle(net.minecraft.ChatFormatting.RED),
                        Component.literal("Construct the final altar."),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .addCriterion(IMPOSSIBLE_NAME, IMPOSSIBLE_CRITERION)
                .save(consumer, BMPOAT_ID.toString());
    }

    private static AdvancementHolder challenge(
            Consumer<AdvancementHolder> consumer,
            AdvancementHolder parent,
            ItemLike icon,
            String title,
            String desc,
            Identifier id
    ) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(icon, Component.literal(title), Component.literal(desc), null,
                        AdvancementType.CHALLENGE, true, true, false)
                .addCriterion(IMPOSSIBLE_NAME, IMPOSSIBLE_CRITERION)
                .save(consumer, id.toString());
    }

    private static AdvancementHolder goal(
            Consumer<AdvancementHolder> consumer,
            AdvancementHolder parent,
            ItemLike icon,
            String title,
            String desc,
            Identifier id
    ) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(icon, Component.literal(title), Component.literal(desc), null,
                        AdvancementType.GOAL, true, true, false)
                .addCriterion(IMPOSSIBLE_NAME, IMPOSSIBLE_CRITERION)
                .save(consumer, id.toString());
    }


    private static MobEffectsPredicate.Builder allEffects() {
        MobEffectsPredicate.Builder builder = MobEffectsPredicate.Builder.effects();

        builder.and(MobEffects.ABSORPTION);
        builder.and(MobEffects.BAD_OMEN);
        builder.and(MobEffects.BLINDNESS);
        builder.and(MobEffects.CONDUIT_POWER);
        builder.and(MobEffects.BREATH_OF_THE_NAUTILUS); // Breath of the Nautilus (same as Conduit Power in code)
        builder.and(MobEffects.DARKNESS);
        builder.and(MobEffects.DOLPHINS_GRACE);
        builder.and(MobEffects.FIRE_RESISTANCE);
        builder.and(MobEffects.GLOWING);
        builder.and(MobEffects.HASTE); // Haste
        builder.and(MobEffects.HERO_OF_THE_VILLAGE);
        builder.and(MobEffects.HUNGER);
        builder.and(MobEffects.INFESTED);
        builder.and(MobEffects.INVISIBILITY);
        builder.and(MobEffects.JUMP_BOOST);
        builder.and(MobEffects.LEVITATION);
        builder.and(MobEffects.MINING_FATIGUE); // Mining Fatigue
        builder.and(MobEffects.NAUSEA); // Nausea
        builder.and(MobEffects.NIGHT_VISION);
        builder.and(MobEffects.OOZING);
        builder.and(MobEffects.POISON);
        builder.and(MobEffects.RAID_OMEN);
        builder.and(MobEffects.REGENERATION);
        builder.and(MobEffects.RESISTANCE); // Resistance
        builder.and(MobEffects.SLOW_FALLING);
        builder.and(MobEffects.SLOWNESS); // Slowness
        builder.and(MobEffects.SPEED); // Speed
        builder.and(MobEffects.STRENGTH); // Strength
        builder.and(MobEffects.TRIAL_OMEN);
        builder.and(MobEffects.WATER_BREATHING);
        builder.and(MobEffects.WEAKNESS);
        builder.and(MobEffects.WEAVING);
        builder.and(MobEffects.WITHER);
        builder.and(MobEffects.WIND_CHARGED);

        return builder;
    }

    private static Identifier mod_id(String str) {
        return Identifier.fromNamespaceAndPath(AOMCUtils.MOD_ID, str);
    }
}
