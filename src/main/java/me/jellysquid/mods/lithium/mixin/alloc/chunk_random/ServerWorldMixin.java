package me.jellysquid.mods.lithium.mixin.alloc.chunk_random;

import me.jellysquid.mods.lithium.common.world.ChunkRandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin {
    private final BlockPos.MutableBlockPos randomPosInChunkCachedPos = new BlockPos.MutableBlockPos();

    /**
     * @reason Avoid allocating BlockPos every invocation through using our allocation-free variant
     */
    @Redirect(
            method = "tickChunk(Lnet/minecraft/world/chunk/WorldChunk;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getRandomPosInChunk(IIII)Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private BlockPos redirectTickGetRandomPosInChunk(ServerLevel serverWorld, int x, int y, int z, int mask) {
        ((ChunkRandomSource) serverWorld).getRandomPosInChunk(x, y, z, mask, this.randomPosInChunkCachedPos);

        return this.randomPosInChunkCachedPos;
    }

    /**
     * @reason Ensure an immutable block position is passed on block tick
     */
    @Redirect(
            method = "tickChunk(Lnet/minecraft/world/chunk/WorldChunk;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V"
            )
    )
    private void redirectBlockStateTick(BlockState blockState, ServerLevel world, BlockPos pos, net.minecraft.util.RandomSource rand) {
        blockState.randomTick(world, pos.immutable(), rand);
    }

    /**
     * @reason Ensure an immutable block position is passed on fluid tick
     */
    @Redirect(
            method = "tickChunk(Lnet/minecraft/world/chunk/WorldChunk;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;onRandomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V"
            )
    )
    private void redirectFluidStateTick(FluidState fluidState, Level world, BlockPos pos, net.minecraft.util.RandomSource rand) {
        fluidState.randomTick(world, pos.immutable(), rand);
    }
}
