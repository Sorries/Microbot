package net.runelite.client.plugins.microbot.sailing;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class SailingOverlay extends OverlayPanel
{
    private final SailingPlugin2 plugin;

    @Inject
    public SailingOverlay(SailingPlugin2 plugin)
    {
        super(plugin);

        this.plugin = plugin;

        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().add(
                TitleComponent.builder()
                        .text("Salvaging")
                        .color(Color.GREEN)
                        .build()
        );

        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left("Runtime:")
                        .right(formatRuntime(System.currentTimeMillis() - plugin.getStartTime()))
                        .build()
        );

        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left("XP gained:")
                        .right(String.valueOf(plugin.getExpGained()))
                        .build()
        );

        return super.render(graphics);
    }

    private String formatRuntime(long millis)
    {
        long seconds = millis / 1000;

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}