package net.runelite.client.plugins.microbot.HelloHello1.WildyHopper;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.shortestpath.WorldPointUtil;
import net.runelite.client.plugins.microbot.util.reachable.Rs2Reachable;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WildyHopperOverlay extends OverlayPanel {

    private static final Color REACHABLE_COLOR = new Color(0, 255, 0, 50);
    private static final Color REACHABLE_BORDER_COLOR = new Color(0, 255, 0, 150);

    @Inject
    private Client client;

    @Inject
    public WildyHopperOverlay() {
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!Microbot.isLoggedIn())
        {
            return null;
        }
        panelComponent.setPreferredSize(new Dimension(200, 300));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("WildyHopper V1.0.0")
                .color(Color.GREEN)
                .build());
//        WorldPoint playerLocation = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
//        if (playerLocation == null)
//        {
//            return null;
//        }
//
//        // Get reachable tiles as packed ints
//        var reachablePacked = Rs2Reachable.getReachableTiles(playerLocation);
//        if (reachablePacked == null || reachablePacked.isEmpty())
//        {
//            return null;
//        }
//
//        // Convert packed ints back to world points in current plane/instance
//        List<WorldPoint> reachableWorldPoints = new ArrayList<>();
//        for (int packed : reachablePacked)
//        {
//            WorldPoint wp = WorldPointUtil.unpackWorldPoint(packed);
//            if (wp.getPlane() == playerLocation.getPlane())
//            {
//                reachableWorldPoints.add(wp);
//            }
//        }
//
//        for (WorldPoint worldPoint : reachableWorldPoints)
//        {
//            LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
//            if (localPoint == null)
//            {
//                continue;
//            }
//
//            Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);
//            if (poly == null)
//            {
//                continue;
//            }
//
//            graphics.setColor(REACHABLE_COLOR);
//            graphics.fillPolygon(poly);
//
//            graphics.setColor(REACHABLE_BORDER_COLOR);
//            graphics.drawPolygon(poly);
//        }
//
//        return null;
        return super.render(graphics);
    }
}
