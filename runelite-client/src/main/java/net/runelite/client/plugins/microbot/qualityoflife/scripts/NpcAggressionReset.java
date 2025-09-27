package net.runelite.client.plugins.microbot.qualityoflife.scripts;

import com.google.inject.Inject;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.MicrobotPlugin;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.qualityoflife.QoLConfig;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldArea;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.npcunaggroarea.NpcAggroAreaPlugin;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.*;




public class NpcAggressionReset extends Script {

    private static final int SAFE_AREA_RADIUS = 10;
    private final WorldPoint[] safeCenters = new WorldPoint[2];
    private final List<WorldPoint> tile = new ArrayList<>();

    public boolean run(QoLConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (!config.npcAggressionReset()) return;
                if (Microbot.isPluginEnabled(NpcAggroAreaPlugin.class)){
                    NpcAggroAreaPlugin plugin = Microbot.getPlugin(NpcAggroAreaPlugin.class);
                    if (plugin != null) {
                        this.safeCenters[0] = plugin.getSafeCenters()[0];
                        this.safeCenters[1] = plugin.getSafeCenters()[1];
                        Microbot.log("Npc safe wp: " + Arrays.toString(this.safeCenters));
                        Microbot.log("Aggression Timer: " + Duration.between(Instant.now(),plugin.getEndTime()).toSeconds());

                        List<WorldArea> safeAreas = generateSafeAreas();
                        Microbot.log("Generated Area: "+ safeAreas);
//                        walkable
                        if (Duration.between(Instant.now(),plugin.getEndTime()).toSeconds() <= 0 ){

                        }
                    } else {
                        Microbot.log("Npc Agro is not running.");
                    }

                }

            } catch(Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private List<WorldArea> generateSafeAreas()
    {
        List<WorldArea> areas = new ArrayList<>();

        for (WorldPoint wp : safeCenters)
        {
            if (wp == null)
            {
                continue;
            }

            int westpoint = wp.getX() - SAFE_AREA_RADIUS;
            int southpoint = wp.getY() - SAFE_AREA_RADIUS;

            areas.add(new WorldArea(
                    westpoint,
                    southpoint,
                    SAFE_AREA_RADIUS * 2,
                    SAFE_AREA_RADIUS * 2,
                    Microbot.getClient().getTopLevelWorldView().getPlane()
            ));
        }
        return areas;
    }

}

