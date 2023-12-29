package me.jellysquid.mods.lithium.common;

import me.jellysquid.mods.lithium.common.config.LithiumConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class LithiumMod extends JavaPlugin {
    public static LithiumConfig CONFIG;

    @Override
    public void onEnable() {
        if (CONFIG == null) {
            throw new IllegalStateException("The mixin plugin did not initialize the config! Did it not load?");
        }
    }
}
