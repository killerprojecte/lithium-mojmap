package me.jellysquid.mods.lithium.common.world;

import net.minecraft.world.entity.Mob;

public interface ServerWorldExtended {
    void setNavigationActive(Mob mobEntity);

    void setNavigationInactive(Mob mobEntity);
}
