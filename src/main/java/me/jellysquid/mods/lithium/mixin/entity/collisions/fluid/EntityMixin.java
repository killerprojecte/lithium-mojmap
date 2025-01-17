package me.jellysquid.mods.lithium.mixin.entity.collisions.fluid;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import me.jellysquid.mods.lithium.common.block.BlockCountingSection;
import me.jellysquid.mods.lithium.common.block.BlockStateFlags;
import me.jellysquid.mods.lithium.common.block.TrackedBlockStatePredicate;
import me.jellysquid.mods.lithium.common.entity.FluidCachingEntity;
import me.jellysquid.mods.lithium.common.util.Pos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public abstract class EntityMixin implements FluidCachingEntity {

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public Level world;

    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Inject(
            method = "updateMovementInFluid(Lnet/minecraft/registry/tag/TagKey;D)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;isPushedByFluids()Z",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void tryShortcutFluidPushing(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir, AABB box, int x1, int x2, int y1, int y2, int z1, int z2, double zero) {
        TrackedBlockStatePredicate blockStateFlag;
        if (tag == FluidTags.WATER) {
            blockStateFlag = BlockStateFlags.WATER;
        } else if (tag == FluidTags.LAVA) {
            blockStateFlag = BlockStateFlags.LAVA;
        } else {
            return;
        }
        int chunkX1 = x1 >> 4;
        int chunkZ1 = z1 >> 4;
        int chunkX2 = ((x2 - 1) >> 4);
        int chunkZ2 = ((z2 - 1) >> 4);
        int chunkYIndex1 = Math.max(Pos.SectionYIndex.fromBlockCoord(this.world, y1), Pos.SectionYIndex.getMinYSectionIndex(this.world));
        int chunkYIndex2 = Math.min(Pos.SectionYIndex.fromBlockCoord(this.world, y2 - 1), Pos.SectionYIndex.getMaxYSectionIndexInclusive(this.world));
        for (int chunkX = chunkX1; chunkX <= chunkX2; chunkX++) {
            for (int chunkZ = chunkZ1; chunkZ <= chunkZ2; chunkZ++) {
                ChunkAccess chunk = this.world.getChunk(chunkX, chunkZ);
                for (int chunkYIndex = chunkYIndex1; chunkYIndex <= chunkYIndex2; chunkYIndex++) {
                    LevelChunkSection section = chunk.getSections()[chunkYIndex];
                    if (((BlockCountingSection) section).mayContainAny(blockStateFlag)) {
                        //fluid found, cannot skip code
                        return;
                    }
                }
            }
        }

        //side effects of not finding a fluid:
        this.fluidHeight.put(tag, 0.0);
        cir.setReturnValue(false);
    }
}
