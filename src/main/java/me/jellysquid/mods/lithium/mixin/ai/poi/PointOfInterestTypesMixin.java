package me.jellysquid.mods.lithium.mixin.ai.poi;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.lithium.common.world.interests.types.PointOfInterestTypeHelper;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

/**
 * Replaces the backing map type with a faster collection type which uses reference equality.
 */
@Mixin(PoiTypes.class)
public class PointOfInterestTypesMixin {
    @Mutable
    @Shadow
    @Final
    private static Map<BlockState, PoiType> POI_STATES_TO_TYPE;

    static {
        POI_STATES_TO_TYPE = new Reference2ReferenceOpenHashMap<>(POI_STATES_TO_TYPE);

        PointOfInterestTypeHelper.init(POI_STATES_TO_TYPE.keySet());
    }
}
