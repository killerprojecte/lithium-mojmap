package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import me.jellysquid.mods.lithium.common.entity.nearby_tracker.NearbyEntityListenerMulti;
import me.jellysquid.mods.lithium.common.entity.nearby_tracker.NearbyEntityListenerProvider;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentEntitySectionManager.class)
public abstract class ServerEntityManagerMixin<T extends EntityAccess> {
    @Shadow
    @Final
    EntitySectionStorage<T> cache;

    @Inject(
            method = "addEntity(Lnet/minecraft/world/entity/EntityLike;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityLike;setChangeListener(Lnet/minecraft/world/entity/EntityChangeListener;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onAddEntity(T entity, boolean existing, CallbackInfoReturnable<Boolean> cir) {
        NearbyEntityListenerMulti listener = ((NearbyEntityListenerProvider) entity).getListener();
        if (listener != null) {
            listener.addToAllChunksInRange(
                    this.cache,
                    SectionPos.of(entity.blockPosition())
            );
        }
    }
}
