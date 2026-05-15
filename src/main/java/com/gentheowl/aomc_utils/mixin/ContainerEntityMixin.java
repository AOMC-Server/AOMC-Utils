package com.gentheowl.aomc_utils.mixin;

import com.gentheowl.aomc_utils.advancement.core.AdvancementPolicies;
import com.gentheowl.aomc_utils.advancement.explore.LootGeneratedChestsAdvancement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerEntity.class)
public interface ContainerEntityMixin {
    @Inject(
            method = "unpackChestVehicleLootTable",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/vehicle/ContainerEntity;setContainerLootTable(Lnet/minecraft/resources/ResourceKey;)V",
                    ordinal = 0
            )
    )
    private void aomcutils_onGeneratedChestVehicleLoot(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp) {
            AdvancementPolicies.get(LootGeneratedChestsAdvancement.class).onGeneratedLoot(sp);
        }
    }
}