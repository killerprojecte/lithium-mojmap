package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.jellysquid.mods.lithium.common.entity.PositionedEntityTrackingSection;
import me.jellysquid.mods.lithium.common.entity.nearby_tracker.NearbyEntityListener;
import me.jellysquid.mods.lithium.common.entity.nearby_tracker.NearbyEntityListenerSection;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.Visibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntitySection.class)
public abstract class EntityTrackingSectionMixin<T extends EntityAccess> implements NearbyEntityListenerSection, PositionedEntityTrackingSection {
    @Shadow
    private Visibility status;
    @Shadow
    @Final
    private ClassInstanceMultiMap<T> collection;

    @Shadow
    public abstract boolean isEmpty();

    private final ReferenceOpenHashSet<NearbyEntityListener> nearbyEntityListeners = new ReferenceOpenHashSet<>(0);

    @Override
    public void addListener(NearbyEntityListener listener) {
        this.nearbyEntityListeners.add(listener);
        if (this.status.isAccessible()) {
            listener.onSectionEnteredRange(this, this.collection);
        }
    }

    @Override
    public void removeListener(EntitySectionStorage<?> sectionedEntityCache, NearbyEntityListener listener) {
        boolean removed = this.nearbyEntityListeners.remove(listener);
        if (this.status.isAccessible() && removed) {
            listener.onSectionLeftRange(this, this.collection);
        }
        if (this.isEmpty()) {
            sectionedEntityCache.remove(this.getPos());
        }
    }

    @Inject(method = "isEmpty()Z", at = @At(value = "HEAD"), cancellable = true)
    public void isEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (!this.nearbyEntityListeners.isEmpty()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "add(Lnet/minecraft/world/entity/EntityLike;)V", at = @At("RETURN"))
    private void onEntityAdded(T entityLike, CallbackInfo ci) {
        if (!this.status.isAccessible() || this.nearbyEntityListeners.isEmpty()) {
            return;
        }
        if (entityLike instanceof Entity entity) {
            for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                nearbyEntityListener.onEntityEnteredRange(entity);
            }
        }
    }

    @Inject(method = "remove(Lnet/minecraft/world/entity/EntityLike;)Z", at = @At("RETURN"))
    private void onEntityRemoved(T entityLike, CallbackInfoReturnable<Boolean> cir) {
        if (this.status.isAccessible() && !this.nearbyEntityListeners.isEmpty() && entityLike instanceof Entity entity) {
            for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                nearbyEntityListener.onEntityLeftRange(entity);
            }
        }
    }

    @ModifyVariable(method = "swapStatus(Lnet/minecraft/world/entity/EntityTrackingStatus;)Lnet/minecraft/world/entity/EntityTrackingStatus;", at = @At(value = "HEAD"), argsOnly = true)
    public Visibility swapStatus(final Visibility newStatus) {
        if (this.status.isAccessible() != newStatus.isAccessible()) {
            if (!newStatus.isAccessible()) {
                if (!this.nearbyEntityListeners.isEmpty()) {
                    for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                        nearbyEntityListener.onSectionLeftRange(this, this.collection);
                    }
                }
            } else {
                if (!this.nearbyEntityListeners.isEmpty()) {
                    for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                        nearbyEntityListener.onSectionEnteredRange(this, this.collection);
                    }
                }
            }
        }
        return newStatus;
    }
}
