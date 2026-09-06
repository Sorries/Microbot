package net.runelite.client.plugins.microbot.sailing;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(SailingConfig.GROUP)
public interface SailingConfig extends Config {

    String GROUP = "micro-sailing";

    @ConfigSection(
            name = "Salvaging",
            description = "Basic salvaging settings",
            position = 0
    )
    String SALVAGING_SECTION = "salvaging";

    @ConfigItem(
            keyName = "salvaging",
            name = "Enable salvaging",
            description = "Enable the basic salvaging loop.",
            position = 0,
            section = SALVAGING_SECTION
    )
    default boolean salvaging() {
        return false;
    }

    @ConfigItem(
            keyName = "enableAlching",
            name = "Enable alching",
            description = "High alch configured items when the inventory is full.",
            position = 1,
            section = SALVAGING_SECTION
    )
    default boolean enableAlching() {
        return false;
    }

    @ConfigItem(
            keyName = "alchItems",
            name = "Alch items",
            description = "Comma-separated item names to high alch.",
            position = 2,
            section = SALVAGING_SECTION
    )
    default String alchItems() {
        return "gold ring, sapphire ring, emerald ring, ruby ring, diamond ring";
    }

    @ConfigItem(
            keyName = "dropItems",
            name = "Drop items",
            description = "Comma-separated item names to drop when the inventory is full.",
            position = 3,
            section = SALVAGING_SECTION
    )
    default String dropItems() {
        return "casket, oyster pearl, oyster pearls, teak logs, steel nails, mithril nails, giant seaweed, mithril cannonball, adamant cannonball, elkhorn frag, plank, oak plank, hemp seed, flax seed, mahogany repair kit, teak repair kit, rum";
    }
}
