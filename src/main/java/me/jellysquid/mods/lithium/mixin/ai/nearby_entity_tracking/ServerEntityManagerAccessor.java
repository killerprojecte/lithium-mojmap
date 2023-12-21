package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PersistentEntitySectionManager.class)
public interface ServerEntityManagerAccessor<T extends EntityAccess> {
    @Accessor
    EntitySectionStorage<T> getCache();
}
