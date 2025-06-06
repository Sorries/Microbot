package net.runelite.client.plugins.microbot.plugindisabler;

import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class PluginDisablerOverlay extends OverlayPanel {
    private final PluginDisablerConfig config;

    @Inject
    PluginDisablerOverlay(PluginDisablerPlugin plugin, PluginDisablerConfig config)
    {
        super(plugin);
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Plugin Disabler" + PluginDisabler.version)
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Minutes since no exp: " + Math.round(PluginDisabler.minutesSinceXpGained))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Maximum minutes defined: " + config.minutes())
                    .build());

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}

