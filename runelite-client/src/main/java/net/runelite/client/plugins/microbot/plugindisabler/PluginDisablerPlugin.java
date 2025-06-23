package net.runelite.client.plugins.microbot.plugindisabler;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.events.ConfigChanged;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Plugin Disabler",
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
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private PluginDisablerOverlay pluginDisablerOverlay;
    @Inject
    private PluginDisablerScript pluginDisabler;

    @Provides
    PluginDisablerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PluginDisablerConfig.class);
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuAction()!= MenuAction.CANCEL) {
            PluginDisablerScript.setLastClickedObjectId(event.getId());
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equalsIgnoreCase("PluginDisabler")) {
            return;
        }
        if (event.getKey().equals("noExp")) {
            if (config.noExp()) {
                PluginDisablerScript.lastXpTime = System.currentTimeMillis();
                Microbot.pauseAllScripts=false;
                PluginDisablerScript.disablePluginsFlag = true;
            }
        }
        if (event.getKey().equals("noClick")) {
            if (config.noClick()) {
                PluginDisablerScript.sameObjectClickCount = 0;
                Microbot.pauseAllScripts=false;
                PluginDisablerScript.disablePluginsFlag = true;
            }
        }
        if (event.getKey().equals("noTime")) {
            if (config.noTime()) {
                pluginDisabler.setStartTime2(System.currentTimeMillis());
                pluginDisabler.setLastTimeConfigValue(-1);
                Microbot.pauseAllScripts=false;
                PluginDisablerScript.disablePluginsFlag = true;
            }
        }
        if (event.getKey().equals("breakHandler")) {
            if (config.useBreaks()) {
                pluginDisabler.setBreakIn(0);
                pluginDisabler.setBreakDuration(0);
                pluginDisabler.scheduleNextBreak();
                Microbot.pauseAllScripts=false;
                PluginDisablerScript.disablePluginsFlag = true;
                PluginDisablerScript.setLockState(false);
            }
            if (!config.useBreaks()) {
                pluginDisabler.setBreakIn(0);
                pluginDisabler.setBreakDuration(0);
                Microbot.pauseAllScripts=false;
                PluginDisablerScript.setLockState(true);
            }
        }
    }

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(pluginDisablerOverlay);
        }
        //pluginDisabler = new PluginDisablerScript(config);
        PluginDisablerScript.lastXpTime = System.currentTimeMillis();
        PluginDisablerScript.disablePluginsFlag = true;
        Microbot.pauseAllScripts = false;
        pluginDisabler.run();
        if (config.useBreaks()) {
            PluginDisablerScript.setLockState(false);
        } else if (!config.useBreaks()) {
            PluginDisablerScript.setLockState(true);
        }

    }
    @Override
    protected void shutDown() {
        Microbot.pauseAllScripts = false;
        overlayManager.remove(pluginDisablerOverlay);
        pluginDisabler.shutdown();
    }
}
