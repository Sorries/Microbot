package net.runelite.client.plugins.microbot.qualityoflife.scripts;

import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.qualityoflife.QoLConfig;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldArea;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.npcunaggroarea.NpcAggroAreaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.util.stream.Collectors;


// npc aggression timer label aggressive zone as safeCenters

public class NpcAggressionReset extends Script {

    private static final int SAFE_AREA_RADIUS = 10;
    private final WorldPoint[] safeCenters = new WorldPoint[2];
    public static List<WorldPoint> walkableTilesAroundSafeAreas;

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
                        //Microbot.log("Npc safe wp: " + Arrays.toString(this.safeCenters));
                        //Microbot.log("Aggression Timer: " + Duration.between(Instant.now(),plugin.getEndTime()).toSeconds());
                        List<WorldArea> safeAreas = generateSafeAreas();
                        //Microbot.log("Generated Area: "+ safeAreas);
                        
                        // Get all walkable tiles surrounding the safe areas
                        walkableTilesAroundSafeAreas = getWalkableTilesAroundSafeAreas(safeAreas);

                        // Remove all tiles that are within the safe areas
                        walkableTilesAroundSafeAreas.removeIf(tile ->
                                safeAreas.stream().anyMatch(safeArea -> safeArea.contains(tile))
                        );

                        // Sort by distanceTo from player's current location
                        // Coordinates also count diagonal as 1 tile so there is usually 3 tiles that are closest at all point
                        WorldPoint playerLocation = Rs2Player.getWorldLocation();
                        walkableTilesAroundSafeAreas.sort((tile1, tile2) ->
                                Integer.compare(
                                        playerLocation.distanceTo(tile1),
                                        playerLocation.distanceTo(tile2)
                                )
                        );

//                        // Sort by Euclidean (sqrt(x^2 +y^2)) distance from player's current location
//                        WorldPoint playerLocation = Rs2Player.getWorldLocation();
//                        walkableTilesAroundSafeAreas.sort((tile1, tile2) -> {
//                            double dist1 = Math.sqrt(Math.pow(playerLocation.getX() - tile1.getX(), 2) +
//                                    Math.pow(playerLocation.getY() - tile1.getY(), 2));
//                            double dist2 = Math.sqrt(Math.pow(playerLocation.getX() - tile2.getX(), 2) +
//                                    Math.pow(playerLocation.getY() - tile2.getY(), 2));
//                            return Double.compare(dist1, dist2);
//                        });

                        //Microbot.log("Walkable tiles around safe areas (sorted by distance): " + walkableTilesAroundSafeAreas);

                        if (Duration.between(Instant.now(),plugin.getEndTime()).toSeconds() <= 0 ){
                            sleep(2000,10000);
                            // Walk to the closest walkable tile
                            WorldPoint originalLocation = Rs2Player.getWorldLocation();
                            int index = Rs2Random.betweenInclusive(0, 6);
                            if (!walkableTilesAroundSafeAreas.isEmpty() && Rs2Tile.isWalkable(walkableTilesAroundSafeAreas.get(index))) {
                                if (Rs2Walker.getWalkPath(walkableTilesAroundSafeAreas.get(index)) != null && !Rs2Walker.getWalkPath(walkableTilesAroundSafeAreas.get(index)).isEmpty()) {
                                    Rs2Walker.walkFastCanvas(walkableTilesAroundSafeAreas.get(index));
                                    Rs2Player.waitForWalking();
                                    sleep(1000,2500);
                                    Rs2Walker.walkTo(originalLocation,3);
                                    Rs2Player.waitForWalking();
                                }
                            }
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
                    (SAFE_AREA_RADIUS * 2) + 1,
                    (SAFE_AREA_RADIUS * 2) + 1,
                    Microbot.getClient().getTopLevelWorldView().getPlane()
            ));
        }
        return areas;
    }

    /**
     * Gets all walkable tiles surrounding the safe areas
     * @param safeAreas List of safe areas to check around
     * @return List of walkable WorldPoints around the safe areas
     */
    private List<WorldPoint> getWalkableTilesAroundSafeAreas(List<WorldArea> safeAreas)
    {
        List<WorldPoint> walkableTiles = new ArrayList<>();
        
        for (WorldArea safeArea : safeAreas)
        {
            // Convert WorldArea to Rs2WorldArea and get surrounding walkable tiles
            Rs2WorldArea Rs2safeArea = new Rs2WorldArea(safeArea);
            List<WorldPoint> surroundingWalkableTiles = Rs2safeArea.getInteractable();
            walkableTiles.addAll(surroundingWalkableTiles);
        }
        
        // Remove duplicates
        return walkableTiles.stream().distinct().collect(Collectors.toList());
    }

}

