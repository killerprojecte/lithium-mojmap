package me.jellysquid.mods.lithium.mixin.entity.inactive_navigations;

import me.jellysquid.mods.lithium.common.entity.NavigatingEntity;
import me.jellysquid.mods.lithium.common.world.ServerWorldExtended;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathNavigation.class)
public abstract class EntityNavigationMixin {

    @Shadow
    @Final
    protected Level world;

    @Shadow
    protected Path currentPath;

    @Shadow
    @Final
    protected Mob entity;

    @Inject(
            method = "recalculatePath()V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/entity/ai/pathing/EntityNavigation;findPathTo(Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/entity/ai/pathing/Path;",
                    shift = At.Shift.AFTER
            )
    )
    private void updateListeningState(CallbackInfo ci) {
        if (((NavigatingEntity) this.entity).isRegisteredToWorld()) {
            if (this.currentPath == null) {
                ((ServerWorldExtended) this.world).setNavigationInactive(this.entity);
            } else {
                ((ServerWorldExtended) this.world).setNavigationActive(this.entity);
            }
        }
    }

    @Inject(method = "startMovingAlong(Lnet/minecraft/entity/ai/pathing/Path;D)Z", at = @At(value = "RETURN"))
    private void updateListeningState2(Path path, double speed, CallbackInfoReturnable<Boolean> cir) {
        if (((NavigatingEntity) this.entity).isRegisteredToWorld()) {
            if (this.currentPath == null) {
                ((ServerWorldExtended) this.world).setNavigationInactive(this.entity);
            } else {
                ((ServerWorldExtended) this.world).setNavigationActive(this.entity);
            }
        }
    }

    @Inject(method = "stop()V", at = @At(value = "RETURN"))
    private void stopListening(CallbackInfo ci) {
        if (((NavigatingEntity) this.entity).isRegisteredToWorld()) {
            ((ServerWorldExtended) this.world).setNavigationInactive(this.entity);
        }
    }
}
