package com.gentheowl.aomc_utils.mixin;

import com.gentheowl.aomc_utils.advancement.LootGeneratedChestsAdvancement;

@Mixin(RandomizableContainer.class)
public interface RandomizableContainerMixin {
    @Inject(method = "unpackLootTable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/RandomizableContainer;setLootTable(Lnet/minecraft/resources/ResourceKey;)V", ordinal = 0))
    private void aomcutils_onGeneratedLoot(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp) {
            LootGeneratedChestsAdvancement.onGeneratedLoot(sp);
        }
    }
}