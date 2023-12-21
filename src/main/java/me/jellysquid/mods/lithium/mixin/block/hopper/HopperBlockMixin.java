package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.common.hopper.UpdateReceiver;
import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntityGetter;
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlock.class)
public abstract class HopperBlockMixin extends BaseEntityBlock {

    protected HopperBlockMixin(Properties settings) {
        super(settings);
    }

    @SuppressWarnings("deprecation")
    @Intrinsic
    @Override
    public BlockState updateShape(BlockState myBlockState, Direction direction, BlockState newState, LevelAccessor world, BlockPos myPos, BlockPos posFrom) {
        return super.updateShape(myBlockState, direction, newState, world, myPos, posFrom);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Inject(method = "getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"))
    private void notifyOnNeighborUpdate(BlockState myBlockState, Direction direction, BlockState newState, LevelAccessor world, BlockPos myPos, BlockPos posFrom, CallbackInfoReturnable<BlockState> ci) {
        //invalidate cache when composters change state
        if (!world.isClientSide() && newState.getBlock() instanceof WorldlyContainerHolder) {
            this.updateHopper(world, myBlockState, myPos, posFrom);
        }
    }

    @Inject(method = "neighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V", at = @At(value = "HEAD"))
    private void updateBlockEntity(BlockState myBlockState, Level world, BlockPos myPos, Block block, BlockPos posFrom, boolean moved, CallbackInfo ci) {
        //invalidate cache when the block is replaced
        if (!world.isClientSide()) {
            this.updateHopper(world, myBlockState, myPos, posFrom);
        }
    }

    private void updateHopper(LevelAccessor world, BlockState myBlockState, BlockPos myPos, BlockPos posFrom) {
        Direction facing = myBlockState.getValue(HopperBlock.FACING);
        boolean above = posFrom.getY() == myPos.getY() + 1;
        if (above || posFrom.getX() == myPos.getX() + facing.getStepX() && posFrom.getY() == myPos.getY() + facing.getStepY() && posFrom.getZ() == myPos.getZ() + facing.getStepZ()) {
            BlockEntity hopper = ((BlockEntityGetter) world).getLoadedExistingBlockEntity(myPos);
            if (hopper instanceof UpdateReceiver updateReceiver) {
                updateReceiver.invalidateCacheOnNeighborUpdate(above);
            }
        }
    }

    @Inject(
            method = "onBlockAdded(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/HopperBlock;updateEnabled(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void workAroundVanillaUpdateSuppression(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        //invalidate caches of nearby hoppers when placing an update suppressed hopper
        if (world.getBlockState(pos) != state) {
            for (Direction direction : UPDATE_SHAPE_ORDER) {
                BlockEntity hopper = ((BlockEntityGetter) world).getLoadedExistingBlockEntity(pos.relative(direction));
                if (hopper instanceof UpdateReceiver updateReceiver) {
                    updateReceiver.invalidateCacheOnNeighborUpdate(direction == Direction.DOWN);
                }
            }
        }
    }
}
