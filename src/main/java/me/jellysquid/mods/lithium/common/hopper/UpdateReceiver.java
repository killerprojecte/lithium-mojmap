package me.jellysquid.mods.lithium.common.hopper;

import net.minecraft.core.Direction;

public interface UpdateReceiver {
    void invalidateCacheOnNeighborUpdate(boolean above);
    void invalidateCacheOnNeighborUpdate(Direction fromDirection);
}
