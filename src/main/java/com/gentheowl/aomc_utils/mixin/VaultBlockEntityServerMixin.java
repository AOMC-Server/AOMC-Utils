package com.gentheowl.aomc_utils.mixin;

import com.gentheowl.aomc_utils.advancement.explore.LootTrialVaultsAdvancement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.gentheowl.aomc_utils.advancement.core.AdvancementPolicies;

@Mixin(targets = "net.minecraft.world.level.block.entity.vault.VaultBlockEntity$Server")
public abstract class VaultBlockEntityServerMixin {
    @Inject(
            method = "tryInsertKey",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/vault/VaultServerData;addToRewardedPlayers(Lnet/minecraft/world/entity/player/Player;)V",
                    shift = At.Shift.AFTER
            )
    )
    private static void aomcutils_onVaultRewarded(
            ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData, Player player, ItemStack itemStack, CallbackInfo ci
    ) {
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
           AdvancementPolicies.get(LootTrialVaultsAdvancement.class).onTrialVaultLooted(sp);
        }
    }
}