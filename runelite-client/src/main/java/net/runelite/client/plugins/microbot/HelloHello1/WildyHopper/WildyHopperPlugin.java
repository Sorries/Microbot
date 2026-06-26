package net.runelite.client.plugins.microbot.HelloHello1.WildyHopper;

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
        name = PluginDescriptor.Default + "Hello WildyHopper",
        description = "Wildy Hopper",
        tags = {"performance", "microbot", "test", "gameobject"},
        enabledByDefault = false
)
public class WildyHopperPlugin extends Plugin {

    @Inject
    private WildyHopperScript wildyHopperScript;

    @Inject
    private WildyHopperOverlay wildyHopperOverlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private WildyHopperConfig wildyHopperConfig;

    @Provides
    WildyHopperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(WildyHopperConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        overlayManager.add(wildyHopperOverlay);
        wildyHopperScript.run();
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(wildyHopperOverlay);
        wildyHopperScript.shutdown();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equalsIgnoreCase("wildyhopper")) {
            return;
        }

        // Handle config changes here
    }
}