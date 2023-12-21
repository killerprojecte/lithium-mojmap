package me.jellysquid.mods.lithium.mixin.util.entity_movement_tracking;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.jellysquid.mods.lithium.common.entity.PositionedEntityTrackingSection;
import me.jellysquid.mods.lithium.common.entity.movement_tracker.EntityMovementTrackerSection;
import me.jellysquid.mods.lithium.common.entity.movement_tracker.MovementTrackerHelper;
import me.jellysquid.mods.lithium.common.entity.movement_tracker.SectionedEntityMovementTracker;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.Visibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(EntitySection.class)
public abstract class EntityTrackingSectionMixin<T extends EntityAccess> implements EntityMovementTrackerSection, PositionedEntityTrackingSection {
    @Shadow
    private Visibility status;

    @Shadow
    public abstract boolean isEmpty();

    private final ReferenceOpenHashSet<SectionedEntityMovementTracker<?, ?>> sectionVisibilityListeners = new ReferenceOpenHashSet<>(0);
    @SuppressWarnings("unchecked")
    private final ArrayList<SectionedEntityMovementTracker<?, ?>>[] entityMovementListenersByType = new ArrayList[MovementTrackerHelper.NUM_MOVEMENT_NOTIFYING_CLASSES];
    private final long[] lastEntityMovementByType = new long[MovementTrackerHelper.NUM_MOVEMENT_NOTIFYING_CLASSES];

    @Override
    public void addListener(SectionedEntityMovementTracker<?, ?> listener) {
        this.sectionVisibilityListeners.add(listener);
        if (this.status.isAccessible()) {
            listener.onSectionEnteredRange(this);
        }
    }

    @Override
    public void removeListener(EntitySectionStorage<?> sectionedEntityCache, SectionedEntityMovementTracker<?, ?> listener) {
        boolean removed = this.sectionVisibilityListeners.remove(listener);
        if (this.status.isAccessible() && removed) {
            listener.onSectionLeftRange(this);
        }
        if (this.isEmpty()) {
            sectionedEntityCache.remove(this.getPos());
        }
    }

    @Override
    public void trackEntityMovement(int notificationMask, long time) {
        long[] lastEntityMovementByType = this.lastEntityMovementByType;
        int size = lastEntityMovementByType.length;
        int mask;
        for (int entityClassIndex = Integer.numberOfTrailingZeros(notificationMask); entityClassIndex < size; ) {
            lastEntityMovementByType[entityClassIndex] = time;

            ArrayList<SectionedEntityMovementTracker<?, ?>> entityMovementListeners = this.entityMovementListenersByType[entityClassIndex];
            if (entityMovementListeners != null) {
                for (int listIndex = entityMovementListeners.size() - 1; listIndex >= 0; listIndex--) {
                    SectionedEntityMovementTracker<?, ?> sectionedEntityMovementTracker = entityMovementListeners.remove(listIndex);
                    sectionedEntityMovementTracker.emitEntityMovement(notificationMask, this);
                }
            }

            mask = 0xffff_fffe << entityClassIndex;
            entityClassIndex = Integer.numberOfTrailingZeros(notificationMask & mask);
        }
    }

    @Override
    public long getChangeTime(int trackedClass) {
        return this.lastEntityMovementByType[trackedClass];
    }

    @Inject(method = "isEmpty()Z", at = @At(value = "HEAD"), cancellable = true)
    public void isEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (!this.sectionVisibilityListeners.isEmpty()) {
            cir.setReturnValue(false);
        }
    }


    @ModifyVariable(method = "swapStatus(Lnet/minecraft/world/entity/EntityTrackingStatus;)Lnet/minecraft/world/entity/EntityTrackingStatus;", at = @At(value = "HEAD"), argsOnly = true)
    public Visibility swapStatus(final Visibility newStatus) {
        if (this.status.isAccessible() != newStatus.isAccessible()) {
            if (!newStatus.isAccessible()) {
                if (!this.sectionVisibilityListeners.isEmpty()) {
                    for (SectionedEntityMovementTracker<?, ?> listener : this.sectionVisibilityListeners) {
                        listener.onSectionLeftRange(this);
                    }
                }
            } else {
                if (!this.sectionVisibilityListeners.isEmpty()) {
                    for (SectionedEntityMovementTracker<?, ?> listener : this.sectionVisibilityListeners) {
                        listener.onSectionEnteredRange(this);
                    }
                }
            }
        }
        return newStatus;
    }

    @Override
    public <S, E extends EntityAccess> void listenToMovementOnce(SectionedEntityMovementTracker<E, S> listener, int trackedClass) {
        if (this.entityMovementListenersByType[trackedClass] == null) {
            this.entityMovementListenersByType[trackedClass] = new ArrayList<>();
        }
        this.entityMovementListenersByType[trackedClass].add(listener);
    }

    @Override
    public <S, E extends EntityAccess> void removeListenToMovementOnce(SectionedEntityMovementTracker<E, S> listener, int trackedClass) {
        if (this.entityMovementListenersByType[trackedClass] != null) {
            this.entityMovementListenersByType[trackedClass].remove(listener);
        }
    }
}
