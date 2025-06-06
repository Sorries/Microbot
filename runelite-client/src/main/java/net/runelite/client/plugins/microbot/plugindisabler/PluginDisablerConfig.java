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
    default double minutes() {
        return 10;
    }

}
