package com.gentheowl.aomc_utils.mixin;

import com.gentheowl.aomc_utils.advancement.core.AdvancementPolicies;
import com.gentheowl.aomc_utils.advancement.explore.FlyDistanceAdvancement;
import com.gentheowl.aomc_utils.advancement.explore.WalkDistanceAdvancement;
import com.gentheowl.aomc_utils.advancement.wealth.*;
import com.gentheowl.aomc_utils.water.WaterDamageHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void aomcutils_onTick(CallbackInfo ci) {
        WaterDamageHandler.onPlayerTick((ServerPlayer) (Object) this);
    }

    @Inject(method = "awardStat", at = @At("TAIL"))
    private void aomcutils_onAwardStat(Stat<?> stat, int amount, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;

        Stat<Identifier> walk = Stats.CUSTOM.get(Stats.WALK_ONE_CM);
        if (stat == walk) {
            AdvancementPolicies.get(WalkDistanceAdvancement.class).onWalkCm(self, amount);
            return;
        }

        Stat<Identifier> aviate = Stats.CUSTOM.get(Stats.AVIATE_ONE_CM);
        if (stat == aviate) {
            AdvancementPolicies.get(FlyDistanceAdvancement.class).onAviateCm(self, amount);
        }
    }

    @Inject(method="openMenu", at=@At("TAIL"))
    private void aomc_utils_onOpenMenu(MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
        ServerPlayer self = (ServerPlayer)(Object)this;

        AbstractContainerMenu menu = self.containerMenu;
        if (menu == null) return;

        AdvancementPolicies.get(ShulkerOfCobbleAdvancement.class).onMenuOpened(self, menu);
        AdvancementPolicies.get(ShulkerOfIronAdvancement.class).onMenuOpened(self, menu);
        AdvancementPolicies.get(ShulkerOfDiamondsAdvancement.class).onMenuOpened(self, menu);
        AdvancementPolicies.get(ShulkerOfDebrisAdvancement.class).onMenuOpened(self, menu);
        AdvancementPolicies.get(ShulkerOfStarAdvancement.class).onMenuOpened(self, menu);
    }
}