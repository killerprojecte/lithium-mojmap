package me.jellysquid.mods.lithium.mixin.entity.replace_entitytype_predicates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GolemRandomStrollInVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

@Mixin(GolemRandomStrollInVillageGoal.class)
public abstract class IronGolemWanderAroundGoalMixin extends RandomStrollGoal {
    public IronGolemWanderAroundGoalMixin(PathfinderMob mob, double speed) {
        super(mob, speed);
    }

    @Shadow
    protected abstract boolean canVillagerSummonGolem(Villager villager);

    @Redirect(
            method = "findVillagerPos",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getEntitiesByType(Lnet/minecraft/util/TypeFilter;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private List<Villager> getEntities(ServerLevel serverWorld, EntityTypeTest<Entity, Villager> filter, AABB box, Predicate<? super Villager> predicate) {
        if (filter == EntityType.VILLAGER) {
            return serverWorld.getEntitiesOfClass(Villager.class, this.mob.getBoundingBox().inflate(32.0), this::canVillagerSummonGolem);
        }
        return serverWorld.getEntities(EntityType.VILLAGER, this.mob.getBoundingBox().inflate(32.0), this::canVillagerSummonGolem);
    }
}
