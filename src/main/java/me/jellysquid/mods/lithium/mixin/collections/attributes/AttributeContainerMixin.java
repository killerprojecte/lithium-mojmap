package me.jellysquid.mods.lithium.mixin.collections.attributes;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

@Mixin(AttributeMap.class)
public class AttributeContainerMixin {
    @Mutable
    @Shadow
    @Final
    private Map<Attribute, AttributeInstance> custom;

    @Mutable
    @Shadow
    @Final
    private Set<AttributeInstance> tracked;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void initCollections(AttributeSupplier defaultAttributes, CallbackInfo ci) {
        this.custom = new Reference2ReferenceOpenHashMap<>(0);
        this.tracked = new ReferenceOpenHashSet<>(0);
    }
}
