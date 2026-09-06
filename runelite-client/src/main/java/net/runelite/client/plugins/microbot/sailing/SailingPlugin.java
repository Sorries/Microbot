package net.runelite.client.plugins.microbot.sailing;

import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.sailing.features.salvaging.SalvagingScript;

import javax.inject.Inject;
import java.awt.AWTException;

/** Native, minimal Sailing salvaging plugin. */
@PluginDescriptor(
        name = PluginDescriptor.Default + "Sailing Salvaging",
        description = "Automates basic sailing salvaging",
        tags = {"sailing", "salvaging", "microbot"},
        enabledByDefault = false
)
public class SailingPlugin extends Plugin {

    static final String VERSION = "1.0.0";

    @Inject
    private SailingConfig config;

    @Inject
    private SailingScript script;

    @Inject
    private SalvagingScript salvagingScript;

    @Provides
    SailingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SailingConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        salvagingScript.register();
        script.run(config);
    }

    @Override
    protected void shutDown() {
        script.shutdown();
        salvagingScript.unregister();
    }
}
