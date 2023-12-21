package me.jellysquid.mods.lithium.mixin.entity.replace_entitytype_predicates;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@Mixin(ArmorStand.class)
public class ArmorStandEntityMixin {
    @Shadow
    @Final
    private static Predicate<Entity> RIDEABLE_MINECART_PREDICATE;

    @Redirect(
            method = "tickCramming()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private List<Entity> getMinecartsDirectly(Level world, Entity excluded, AABB box, Predicate<? super Entity> predicate) {
        if (predicate == RIDEABLE_MINECART_PREDICATE) {
            // Not using MinecartEntity.class and no predicate, because mods may add another minecart that is type rideable without being MinecartEntity
            //noinspection unchecked,rawtypes
            return (List) world.getEntitiesOfClass(AbstractMinecart.class, box, (Entity e) -> e != excluded && ((AbstractMinecart) e).getMinecartType() == AbstractMinecart.Type.RIDEABLE);
        }

        return world.getEntities(excluded, box, predicate);
    }
}
