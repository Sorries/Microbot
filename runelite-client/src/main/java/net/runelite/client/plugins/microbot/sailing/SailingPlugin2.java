package net.runelite.client.plugins.microbot.sailing;

import com.google.inject.Provides;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.sailing.features.salvaging.SalvagingScript;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import javax.inject.Inject;
import java.awt.AWTException;

/** Native, minimal Sailing salvaging plugin. */
@PluginDescriptor(
        name = PluginDescriptor.Default + "Sailing Salvaging",
        description = "Automates basic sailing salvaging",
        tags = {"sailing", "salvaging", "microbot"},
        enabledByDefault = false
)
public class SailingPlugin2 extends Plugin {

    static final String VERSION = "1.0.0";

    @Inject
    private SailingConfig config;

    @Inject
    private SailingScript script;

    @Inject
    private SalvagingScript salvagingScript;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private SailingOverlay overlay;

    private long startTime;
    private int startXp;

    @Provides
    SailingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SailingConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        startTime = System.currentTimeMillis();
        startXp = Microbot.getClient().getSkillExperience(Skill.SAILING);
        overlayManager.add(overlay);
        salvagingScript.register();
        script.run(config);
    }

    public long getStartTime()
    {
        return startTime;
    }

    public int getExpGained()
    {
        return Microbot.getClient().getSkillExperience(Skill.SAILING) - startXp;
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        script.shutdown();
        salvagingScript.unregister();
        salvagingScript.shutdown();
    }
}
