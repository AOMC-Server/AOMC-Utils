package com.gentheowl.aomc_utils.advancement.peak;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.advancement.core.SimpleAdvancement;
import com.gentheowl.aomc_utils.datagen.ModAdvancements;
import com.gentheowl.aomc_utils.mixin.StructureTemplateAccessor;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public final class BMPOATAdvancement extends SimpleAdvancement {
    private static final Identifier TEMPLATE_ID = Identifier.fromNamespaceAndPath(AOMCUtils.MOD_ID, "olympus");

    private final MinecraftServer server;
    private final AdvancementHolder gate;

    public BMPOATAdvancement(MinecraftServer server) {
        this.server = server;
        this.gate = server.getAdvancements().get(ModAdvancements.OLYMPUS_GATE_ID);
    }

    @Override
    protected AdvancementHolder advancement() {
        return summit();
    }

    @Override
    public void register() {
        // Backfill support: players who already finished all 3 branches before this existed.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, srv) -> grantGateIfReady(handler.getPlayer()));

        // Temple check lives here
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

            BlockPos beaconPos = hit.getBlockPos();
            if (!world.getBlockState(beaconPos).is(Blocks.BEACON)) return InteractionResult.PASS;

            // Summit should only be actionable once the gate is granted (i.e., all 3 branches done).
            if (!isGateGranted(sp)) {
                return InteractionResult.PASS;
            }

            if (advancement() == null || hasThis(sp)) return InteractionResult.PASS;

            if (matchesOlympus((ServerLevel) world, beaconPos)) {
                spawnEgg((ServerLevel) world, beaconPos);
                complete(sp);
                return InteractionResult.CONSUME;
            }

            return InteractionResult.PASS;
        });
    }

    public void onBranchEnd(ServerPlayer player) {
        grantGateIfReady(player);
    }

    private void grantGateIfReady(ServerPlayer player) {
        AdvancementHolder gate = gate();
        if (gate == null) return;

        if (player.getAdvancements().getOrStartProgress(gate).isDone()) return;

        if (isDone(player, ModAdvancements.KILL_MANY_MOBS_ID)
                && isDone(player, ModAdvancements.VISIT_STRONGHOLDS_ID)
                && isDone(player, ModAdvancements.SHULKER_STAR_ID)) {

            player.getAdvancements().award(gate, criterion());

            // force immediate client sync (belt + suspenders)
            player.getAdvancements().flushDirty(player, true);
        }
    }

    private AdvancementHolder gate() {
        return server.getAdvancements().get(ModAdvancements.OLYMPUS_GATE_ID);
    }

    private AdvancementHolder summit() {
        return server.getAdvancements().get(ModAdvancements.BMPOAT_ID);
    }

    private boolean isGateGranted(ServerPlayer player) {
        return gate != null && player.getAdvancements().getOrStartProgress(gate).isDone();
    }

    private boolean isDone(ServerPlayer player, Identifier id) {
        AdvancementHolder h = server.getAdvancements().get(id);
        return h != null && player.getAdvancements().getOrStartProgress(h).isDone();
    }

    private static final BlockPos BEACON_LOCAL = new BlockPos(5, 1, 5);

    private boolean matchesOlympus(ServerLevel level, BlockPos clickedBeaconPos) {
        StructureTemplate template = level.getStructureManager().getOrCreate(TEMPLATE_ID);
        StructurePlaceSettings settings = new StructurePlaceSettings(); // no rotation/mirror

        var palettes = ((StructureTemplateAccessor) template).aomc$getPalettes();
        if (palettes.isEmpty()) return false;

        StructureTemplate.Palette palette = settings.getRandomPalette(palettes, clickedBeaconPos);
        var infos = palette.blocks();

        BlockPos origin = clickedBeaconPos.subtract(BEACON_LOCAL);

        BlockPos.MutableBlockPos wp = new BlockPos.MutableBlockPos();
        for (var info : infos) {
            Block expected = info.state().getBlock();
            if (expected == Blocks.AIR || expected == Blocks.STRUCTURE_VOID) continue;

            BlockPos lp = info.pos();
            wp.set(origin.getX() + lp.getX(),
                    origin.getY() + lp.getY(),
                    origin.getZ() + lp.getZ());

            if (level.getBlockState(wp).getBlock() != expected) return false;
        }

        return true;
    }

    private void spawnEgg(ServerLevel level, BlockPos beaconPos) {
        DragonSpawnEggSequence.start(level, beaconPos);
    }
}