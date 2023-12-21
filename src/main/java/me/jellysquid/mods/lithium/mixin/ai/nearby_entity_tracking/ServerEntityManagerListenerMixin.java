package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import me.jellysquid.mods.lithium.common.entity.nearby_tracker.NearbyEntityListenerMulti;
import me.jellysquid.mods.lithium.common.entity.nearby_tracker.NearbyEntityListenerProvider;
import me.jellysquid.mods.lithium.common.util.tuples.Range6Int;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.entity.Visibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net/minecraft/server/world/ServerEntityManager$Listener")
public class ServerEntityManagerListenerMixin<T extends EntityAccess> {
    @Shadow
    @Final
    private T entity;

    @Final
    @SuppressWarnings("ShadowTarget")
    @Shadow
    PersistentEntitySectionManager<T> manager;

    @Shadow
    private long sectionPos;

    @Inject(
            method = "updateEntityPosition()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityTrackingSection;add(Lnet/minecraft/world/entity/EntityLike;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onUpdateEntityPosition(CallbackInfo ci, BlockPos blockPos, long newPos, Visibility entityTrackingStatus, EntitySection<T> entityTrackingSection) {
        NearbyEntityListenerMulti listener = ((NearbyEntityListenerProvider) this.entity).getListener();
        if (listener != null)
        {
            Range6Int chunkRange = listener.getChunkRange();
            //noinspection unchecked
            listener.updateChunkRegistrations(
                    ((ServerEntityManagerAccessor<T>) this.manager).getCache(),
                    SectionPos.of(this.sectionPos), chunkRange,
                    SectionPos.of(newPos), chunkRange
            );
        }
    }

    @Inject(
            method = "remove(Lnet/minecraft/entity/Entity$RemovalReason;)V",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onRemoveEntity(Entity.RemovalReason reason, CallbackInfo ci) {
        NearbyEntityListenerMulti listener = ((NearbyEntityListenerProvider) this.entity).getListener();
        if (listener != null) {
            //noinspection unchecked
            listener.removeFromAllChunksInRange(
                    ((ServerEntityManagerAccessor<T>) this.manager).getCache(),
                    SectionPos.of(this.sectionPos)
            );
        }
    }
}
