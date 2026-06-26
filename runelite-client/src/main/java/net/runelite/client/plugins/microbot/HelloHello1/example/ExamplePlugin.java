package net.runelite.client.plugins.microbot.HelloHello1.example;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
@Slf4j
@PluginDescriptor(
        name = PluginDescriptor.Default + "Hello Example Plugin",
        description = "Hello Example Plugin - Testing",
        tags = {"performance", "microbot", "test", "gameobject"},
        enabledByDefault = false
)
public class ExamplePlugin extends Plugin {

    @Inject
    private ExampleScript exampleScript;

    @Inject
    private ExampleScriptOverlay exampleScriptOverlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ExampleConfig exampleConfig;

    @Provides
    ExampleConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ExampleConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        overlayManager.add(exampleScriptOverlay);
        exampleScript.run();
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(exampleScriptOverlay);
        exampleScript.shutdown();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("example")) {
            return;
        }

        // Handle config changes here
    }
}