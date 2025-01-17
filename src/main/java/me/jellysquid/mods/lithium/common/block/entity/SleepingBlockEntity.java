package me.jellysquid.mods.lithium.common.block.entity;

import me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping.WrappedBlockEntityTickInvokerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

public interface SleepingBlockEntity {
    TickingBlockEntity SLEEPING_BLOCK_ENTITY_TICKER = new TickingBlockEntity() {
        public void tick() {
        }

        public boolean isRemoved() {
            return false;
        }

        public BlockPos getPos() {
            return null;
        }

        public String getType() {
            return "<lithium_sleeping>";
        }
    };

    WrappedBlockEntityTickInvokerAccessor getTickWrapper();

    void setTickWrapper(WrappedBlockEntityTickInvokerAccessor tickWrapper);

    TickingBlockEntity getSleepingTicker();

    void setSleepingTicker(TickingBlockEntity sleepingTicker);

    default boolean startSleeping() {
        if (this.isSleeping()) {
            return false;
        }

        WrappedBlockEntityTickInvokerAccessor tickWrapper = this.getTickWrapper();
        if (tickWrapper == null) {
            return false;
        }
        this.setSleepingTicker(tickWrapper.getWrapped());
        tickWrapper.callSetWrapped(SleepingBlockEntity.SLEEPING_BLOCK_ENTITY_TICKER);
        return true;
    }

    default void sleepOnlyCurrentTick() {
        TickingBlockEntity sleepingTicker = this.getSleepingTicker();
        WrappedBlockEntityTickInvokerAccessor tickWrapper = this.getTickWrapper();
        if (sleepingTicker == null) {
            sleepingTicker = tickWrapper.getWrapped();
        }
        Level world = ((BlockEntity) this).getLevel();
        tickWrapper.callSetWrapped(new SleepUntilTimeBlockEntityTickInvoker((BlockEntity) this, world.getGameTime() + 1, sleepingTicker));
        this.setSleepingTicker(null);
    }

    default void wakeUpNow() {
        TickingBlockEntity sleepingTicker = this.getSleepingTicker();
        if (sleepingTicker == null) {
            return;
        }
        this.setTicker(sleepingTicker);
        this.setSleepingTicker(null);
    }

    default void setTicker(TickingBlockEntity delegate) {
        WrappedBlockEntityTickInvokerAccessor tickWrapper = this.getTickWrapper();
        if (tickWrapper == null) {
            return;
        }
        tickWrapper.callSetWrapped(delegate);
    }

    default boolean isSleeping() {
        return this.getSleepingTicker() != null;
    }
}
