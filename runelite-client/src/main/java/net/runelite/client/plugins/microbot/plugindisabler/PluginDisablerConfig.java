package net.runelite.client.plugins.microbot.plugindisabler;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("PluginDisabler")
public interface PluginDisablerConfig extends Config {

    @ConfigItem(
            name = "Pause all plugins after X min of no exp",
            keyName = "Pause all plugins after X min of no exp",
            position = 0,
            description = "Pause all plugins after X min of no exp"
    )
    default boolean noExp() {
        return true;
    }

    @ConfigItem(
            name = "How many X minutes to check",
            keyName = "How many minutes to check",
            position = 1,
            description = "How many minutes to check before shutting off"
    )
    default int minutes() {
        return 5;
    }
    @ConfigItem(
            name = "Pause all plugins after X amount of clicks",
            keyName = "Pause all plugins after X amount of click",
            position = 2,
            description = "Pause all plugins after X amount of clicks"
    )
    default boolean noClick() {
        return true;
    }

    @ConfigItem(
            name = "How many X clicks to check",
            keyName = "How many clicks to check",
            position = 3,
            description = "How many clicks to check before shutting off"
    )
    default int clicks() {
        return 30;
    }
}
