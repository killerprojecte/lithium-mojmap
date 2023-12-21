package me.jellysquid.mods.lithium.mixin.world.inline_height;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelChunk.class)
public abstract class WorldChunkMixin implements LevelHeightAccessor {

    @Shadow
    @Final
    Level world;

    @Override
    public int getMaxBuildHeight() {
        return this.world.getMaxBuildHeight();
    }

    @Override
    public int getSectionsCount() {
        return this.world.getSectionsCount();
    }

    @Override
    public int getMinSection() {
        return this.world.getMinSection();
    }

    @Override
    public int getMaxSection() {
        return this.world.getMaxSection();
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return this.world.isOutsideBuildHeight(pos);
    }

    @Override
    public boolean isOutsideBuildHeight(int y) {
        return this.world.isOutsideBuildHeight(y);
    }

    @Override
    public int getSectionIndex(int y) {
        return this.world.getSectionIndex(y);
    }

    @Override
    public int getSectionIndexFromSectionY(int coord) {
        return this.world.getSectionIndexFromSectionY(coord);
    }

    @Override
    public int getSectionYFromSectionIndex(int index) {
        return this.world.getSectionYFromSectionIndex(index);
    }
}
