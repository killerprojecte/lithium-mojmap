package me.jellysquid.mods.lithium.mixin.chunk.entity_class_groups;

import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.level.entity.EntitySection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySection.class)
public interface EntityTrackingSectionAccessor<T> {
    @Accessor("collection")
    ClassInstanceMultiMap<T> getCollection();
}