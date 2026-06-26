package net.runelite.client.plugins.microbot.HelloHello1.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface ExampleConfig extends Config {

    @ConfigItem(
            keyName = "debug",
            name = "Debug",
            description = "Enable debug messages",
            position = 0
    )
    default boolean debug() {
        return false;
    }

}
