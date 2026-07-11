package com.gentheowl.aomc_utils.mixin;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.debug.RestartDetectorCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Clock;
import java.time.LocalTime;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER;

@Mixin(DaylightDetectorBlock.class)
public abstract class DaylightDetectorMixin {
    @Unique
    private static final int TARGET_HOUR = 23;
    @Unique
    private static final int TARGET_MINUTE = 45;

    @Shadow
    private static void updateSignalStrength(BlockState state, Level world, BlockPos pos) {
        throw new AssertionError();
    }

    @Inject(method = "getTicker", at = @At("RETURN"), cancellable = true)
    private <T extends BlockEntity> void aomc$tickWithoutSkylight(Level world, BlockState state, BlockEntityType<T> type,
                                                                  CallbackInfoReturnable<BlockEntityTicker<T>> cir) {
        if (cir.getReturnValue() != null) return;
        if (world.isClientSide()) return;
        if (type != BlockEntityTypes.DAYLIGHT_DETECTOR) return;

        cir.setReturnValue((level, pos, blockState, blockEntity) -> {
            if (level.getGameTime() % 20L != 0L) return;
            boolean onNetherite = level.getBlockState(pos.below()).is(Blocks.NETHERITE_BLOCK);
            if (onNetherite || blockState.getValue(POWER) > 0) {
                updateSignalStrength(blockState, level, pos);
            }
        });
    }

    @Inject(method = "updateSignalStrength", at = @At("HEAD"), cancellable = true)
    private static void beforeStateUpdate(BlockState state, Level world, BlockPos pos, CallbackInfo ci) {
        if (world.isClientSide()) return;

        if (world.getBlockState(pos.below()).is(Blocks.NETHERITE_BLOCK)) {
            Boolean forced = RestartDetectorCommand.override();
            if (forced != null) {
                world.setBlock(pos, state.setValue(POWER, forced ? 15 : 0), DaylightDetectorBlock.UPDATE_ALL);
                ci.cancel();
                return;
            }

            LocalTime now = LocalTime.now(Clock.systemUTC());
            boolean standardWindow = (now.getHour() == TARGET_HOUR && now.getMinute() >= TARGET_MINUTE);
            boolean specialWindow = (now.getHour() == 0 && now.getMinute() < 5);

            if (!(standardWindow || specialWindow)) {
                world.setBlock(pos, state.setValue(POWER, 0), DaylightDetectorBlock.UPDATE_ALL);
                ci.cancel();
                return;
            }

            int minutesActive;
            if (standardWindow) {
                minutesActive = now.getMinute() - TARGET_MINUTE;
            } else { // specialWindow
                minutesActive = 14;
            }

            int power = Math.min(minutesActive+1, 15);
            world.setBlock(pos, state.setValue(POWER, power), DaylightDetectorBlock.UPDATE_ALL);
            ci.cancel();
        }
    }
}
