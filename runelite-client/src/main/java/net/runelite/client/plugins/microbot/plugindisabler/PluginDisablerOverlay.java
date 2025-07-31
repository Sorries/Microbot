package net.runelite.client.plugins.microbot.plugindisabler;

import net.runelite.client.plugins.microbot.Microbot;
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
            panelComponent.setPreferredSize(new Dimension(225, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Plugin Disabler" + PluginDisablerScript.version)
                    .color(Color.GREEN)
                    .build());
            if(config.useBreaks()) {
                panelComponent.getChildren().add(LineComponent.builder().build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Break in: " + (PluginDisablerScript.getInstance().getBreakIn() / 60) + " min")
                        .build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Break duration: " + (PluginDisablerScript.getInstance().getBreakDuration() / 60) + " min")
                        .build());

                if (PluginDisablerScript.getInstance().getBreakDuration()>0){
                    panelComponent.getChildren().add(LineComponent.builder().build());
                    panelComponent.getChildren().add(TitleComponent.builder()
                            .text("Currently Breaking")
                            .color(Color.GREEN)
                            .build());
                }
            }
            if (config.noExp()) {
                panelComponent.getChildren().add(LineComponent.builder().build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Minutes since no exp: " + Math.round(PluginDisablerScript.minutesSinceXpGained))
                        .build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Maximum minutes allow: " + config.minutes())
                        .build());
            }
            if (config.noClick()){
                panelComponent.getChildren().add(LineComponent.builder().build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Number of duplicate clicks: " + PluginDisablerScript.sameObjectClickCount)
                        .build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Maximum duplicate clicks allow: " + config.clicks())
                        .build());
            }
            if (config.cantReach()){
                panelComponent.getChildren().add(LineComponent.builder().build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Number of 'can't reach': " + PluginDisablerScript.getInstance().cantReachTimestamps.size())
                        .build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Maximum number of 'can't reach': " + config.cantReachNumber())
                        .build());
            }
            if (config.noTime()) {
                panelComponent.getChildren().add(LineComponent.builder().build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Number of minutes before shutoff: " + PluginDisablerScript.minutesLeft)
                        .build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Maximum minutes before shutoff: " + PluginDisablerScript.timeThresholdMinutes)
                        .build());
            }

            if (PluginDisablerScript.disablePluginsFlag){panelComponent.getChildren().add(LineComponent.builder().build());
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Enabled")
                    .color(Color.GREEN)
                    .build());
            }else{
                panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Disabled")
                    .color(Color.RED)
                    .build());
            }

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}

