package me.jellysquid.mods.lithium.mixin.ai.poi.fast_portals;

import me.jellysquid.mods.lithium.common.util.POIRegistryEntries;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestStorageExtended;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @Shadow
    @Final
    private ServerLevel world;

    /**
     * @author JellySquid
     * @reason Use optimized search for nearby points, avoid slow filtering, check for valid locations first
     * [VanillaCopy] everything but the Optional<PointOfInterest> lookup
     */
    @Overwrite
    public Optional<BlockUtil.FoundRectangle> getPortalRect(BlockPos centerPos, boolean dstIsNether, WorldBorder worldBorder) {
        int searchRadius = dstIsNether ? 16 : 128;

        PoiManager poiStorage = this.world.getPoiManager();
        poiStorage.ensureLoadedAndValid(this.world, centerPos, searchRadius);

        Optional<PoiRecord> ret = ((PointOfInterestStorageExtended) poiStorage).findNearestForPortalLogic(centerPos, searchRadius,
                POIRegistryEntries.NETHER_PORTAL_ENTRY, PoiManager.Occupancy.ANY,
                (poi) -> this.world.getBlockState(poi.getPos()).hasProperty(BlockStateProperties.HORIZONTAL_AXIS),
                worldBorder
        );

        return ret.map(poi -> {
            BlockPos blockPos = poi.getPos();
            this.world.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockPos), 3, blockPos);
            BlockState blockState = this.world.getBlockState(blockPos);
            return BlockUtil.getLargestRectangleAround(blockPos, blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, pos -> this.world.getBlockState(pos) == blockState);
        });
    }
}
