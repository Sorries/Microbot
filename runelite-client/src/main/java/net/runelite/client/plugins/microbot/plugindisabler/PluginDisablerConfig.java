package net.runelite.client.plugins.microbot.plugindisabler;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("PluginDisabler")
public interface PluginDisablerConfig extends Config {

    @ConfigItem(
            name = "Break Handler",
            keyName = "breakHandler",
            position = 1,
            description = "Break handle"
    )
    default boolean useBreaks() {
        return true;
    }

    @ConfigItem(
            name = "Min Playtime",
            keyName = "minPlaytime",
            position = 2,
            description = ""
    )
    default int minPlaytime() {
        return 20;
    }

    @ConfigItem(
            name = "Max Playtime",
            keyName = "maxPlaytime",
            position = 3,
            description = ""
    )
    default int maxPlaytime() {
        return 40;
    }

    @ConfigItem(
            name = "Min Breaktime",
            keyName = "minBreaktime",
            position = 4,
            description = ""
    )
    default int minBreaktime() {
        return 3;
    }

    @ConfigItem(
            name = "Max Breaktime",
            keyName = "maxBreaktime",
            position = 5,
            description = ""
    )
    default int maxBreaktime() {
        return 8;
    }

    @ConfigItem(
            name = "Pause all plugins after X min of no exp",
            keyName = "noExp",
            position = 6,
            description = "Pause all plugins after X min of no exp"
    )
    default boolean noExp() {
        return true;
    }

    @ConfigItem(
            name = "How many X minutes to check",
            keyName = "minutes",
            position = 7,
            description = "How many minutes to check before shutting off"
    )
    default int minutes() {
        return 5;
    }

    @ConfigItem(
            name = "Pause all plugins after X amount of clicks",
            keyName = "noClick",
            position = 8,
            description = "Pause all plugins after X amount of clicks"
    )
    default boolean noClick() {
        return true;
    }

    @ConfigItem(
            name = "How many X clicks to check",
            keyName = "clicks",
            position = 9,
            description = "How many clicks to check before shutting off"
    )
    default int clicks() {
        return 30;
    }

    @ConfigItem(
            name = "Stop all plugins after X amount of time",
            keyName = "noTime",
            position = 10,
            description = "Pause all plugins after X amount of time"
    )
    default boolean noTime() {
        return true;
    }

    @ConfigItem(
            name = "How many X minutes to check",
            keyName = "time",
            position = 11,
            description = "How many X minutes to check before shutting off"
    )
    default int time() {
        return 120;
    }

    @ConfigItem(
            name = "Teleport out after disabling plugin",
            keyName = "teleOut",
            position = 12,
            description = "Teleport out after disabling plugin"
    )
    default boolean teleOut() {
        return true;
    }


}
