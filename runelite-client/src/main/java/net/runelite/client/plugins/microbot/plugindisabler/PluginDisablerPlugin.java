package net.runelite.client.plugins.microbot.plugindisabler;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Plugin Disabler</html>",
        description = "Plugin Disabler",
        tags = {"plugin","disable","random", "events", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class PluginDisablerPlugin extends Plugin {
    @Inject
    private ConfigManager configManager;
    @Inject
    private PluginDisablerConfig config;

    private PluginDisabler pluginDisabler;

    @Provides
    PluginDisablerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PluginDisablerConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        pluginDisabler = new PluginDisabler(config);
        Microbot.getBlockingEventManager().add(pluginDisabler);
        Microbot.log("St");
    }

    protected void shutDown() {
        Microbot.getBlockingEventManager().remove(pluginDisabler);
        pluginDisabler = null;
        Microbot.pauseAllScripts = false;
        Microbot.log("Sh");
    }
}
