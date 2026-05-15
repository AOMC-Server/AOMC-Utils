package com.gentheowl.aomc_utils.mixin;

import com.gentheowl.aomc_utils.advancement.core.AdvancementPolicies;
import com.gentheowl.aomc_utils.advancement.explore.LootGeneratedChestsAdvancement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomizableContainer.class)
public interface RandomizableContainerMixin {
    @Inject(method = "unpackLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/RandomizableContainer;setLootTable(Lnet/minecraft/resources/ResourceKey;)V", ordinal = 0))
    private void aomcutils_onGeneratedLoot(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp) {
            AdvancementPolicies.get(LootGeneratedChestsAdvancement.class).onGeneratedLoot(sp);
        }
    }
}