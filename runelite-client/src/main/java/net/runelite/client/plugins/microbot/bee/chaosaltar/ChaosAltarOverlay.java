package net.runelite.client.plugins.microbot.bee.chaosaltar;

import com.google.inject.Inject;
import net.runelite.client.plugins.microbot.runecrafting.ourania.OuraniaScript;
import net.runelite.client.plugins.microbot.util.player.Rs2Pvp;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

import java.awt.*;
import java.time.Duration;

public class ChaosAltarOverlay extends OverlayPanel {
    @Inject
    private ChaosAltarScript chaosAltarScript;
    @Inject
    private ChaosAltarPlugin chaosAltarPlugin;
    private final PanelComponent panelComponent = new PanelComponent();


    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().clear();

        // Header
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Chaos Altar Bot")
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Run time:")
                .right(getFormattedDuration(chaosAltarPlugin.getStartTime()))
                .build());
        return panelComponent.render(graphics);
    }

    private String getFormattedDuration(Duration duration)
    {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
