package net.runelite.client.plugins.microbot.sailing.features.salvaging;

import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.api.tileobject.models.Rs2TileObjectModel;
import net.runelite.client.plugins.microbot.sailing.SailingConfig;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Singleton
public class SalvagingScript {

    private static final int SALVAGE_RANGE = 15;
    private static final int INVENTORY_THRESHOLD = 24;
    private static final int ACTION_TIMEOUT_MS = 5_000;
    private static final int DEPOSIT_TIMEOUT_MS = 20_000;

    private final EventBus eventBus;
    private volatile List<Rs2TileObjectModel> activeWrecks = List.of();

    @Inject
    public SalvagingScript(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void register() {
        eventBus.register(this);
    }

    public void unregister() {
        eventBus.unregister(this);
    }

    /** Collects wrecks from the top-level sea view for the background script. */
    @Subscribe
    public void onGameTick(GameTick event) {
        Map<String, Rs2TileObjectModel> wrecks = new LinkedHashMap<>();
        Client client = Microbot.getClient();
        if (client == null) {
            activeWrecks = List.of();
            return;
        }

        WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null || worldView.getScene() == null) {
            return;
        }

        Tile[][][] tiles = worldView.getScene().getTiles();
        int plane = worldView.getPlane();
        if (tiles == null || plane < 0 || plane >= tiles.length || tiles[plane] == null) {
            return;
        }

        Tile[][] planeTiles = tiles[plane];
        for (int x = 0; x < Math.min(Constants.SCENE_SIZE, planeTiles.length); x++) {
            Tile[] column = planeTiles[x];
            if (column == null) {
                continue;
            }
            for (int y = 0; y < Math.min(Constants.SCENE_SIZE, column.length); y++) {
                Tile tile = column[y];
                if (tile == null) {
                    continue;
                }
                GameObject[] gameObjects = tile.getGameObjects();
                if (gameObjects != null) {
                    for (GameObject object : gameObjects) {
                        if (object != null && object.getSceneMinLocation().equals(tile.getSceneLocation())) {
                            addWreck(object, wrecks);
                        }
                    }
                }
                DecorativeObject decorativeObject = tile.getDecorativeObject();
                if (decorativeObject != null) {
                    addWreck(decorativeObject, wrecks);
                }
            }
        }

        activeWrecks = List.copyOf(wrecks.values());
    }

    private void addWreck(TileObject object, Map<String, Rs2TileObjectModel> wrecks) {
        if (!SalvageObjectIds.ACTIVE_SHIPWRECK_IDS.contains(object.getId())) {
            return;
        }

        WorldPoint location = object.getWorldLocation();
        String key = object.getId() + ":" + location.getX() + ":" + location.getY() + ":" + location.getPlane();
        wrecks.put(key, new Rs2TileObjectModel(object));
    }

    public void run(SailingConfig config) {
        if (Rs2Player.isAnimating()) {
            return;
        }

        if (Rs2Inventory.count() >= INVENTORY_THRESHOLD) {
            clearInventory(config);
            return;
        }

        if (nearestActiveWreck(Rs2Player.getWorldLocation()) != null) {
            deployHook();
        }
    }

    private Rs2TileObjectModel nearestActiveWreck(WorldPoint playerLocation) {
        if (playerLocation == null) {
            return null;
        }

        return activeWrecks.stream()
                .filter(wreck -> playerLocation.distanceTo(wreck.getWorldLocation()) <= SALVAGE_RANGE)
                .min(Comparator.comparingInt(wreck -> playerLocation.distanceTo(wreck.getWorldLocation())))
                .orElse(null);
    }

    private void clearInventory(SailingConfig config) {
        if (Rs2Inventory.count("salvage") > 0) {
            Rs2TileObjectModel station = Microbot.getRs2TileObjectCache().query()
                    .fromWorldView()
                    .where(object -> SalvagingStationObjectIds.ALL_IDS.contains(object.getId()))
                    .nearestOnClientThread();
            if (station == null) {
                return;
            }
            station.click();
            sleepUntil(() -> Rs2Inventory.count("salvage") == 0, DEPOSIT_TIMEOUT_MS);
            return;
        }

        dropConfiguredItems(config);
        alchConfiguredItems(config);
        dropConfiguredItems(config);
    }

    private void deployHook() {
        Rs2TileObjectModel hook = Microbot.getRs2TileObjectCache().query()
                .fromWorldView()
                .where(object -> object.getName() != null
                        && object.getName().toLowerCase().contains("salvaging hook"))
                .nearestOnClientThread();
        if (hook == null) {
            return;
        }

        hook.click("Deploy");
        sleepUntil(Rs2Player::isAnimating, ACTION_TIMEOUT_MS);
    }

    private void dropConfiguredItems(SailingConfig config) {
        String configuredItems = config.dropItems();
        if (configuredItems == null || configuredItems.isBlank()) {
            return;
        }

        String[] itemNames = Arrays.stream(configuredItems.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toArray(String[]::new);
        if (itemNames.length > 0) {
            Rs2Inventory.dropAll(itemNames);
        }
    }

    private void alchConfiguredItems(SailingConfig config) {
        if (!config.enableAlching() || config.alchItems() == null || config.alchItems().isBlank()) {
            return;
        }

        List<String> itemNames = new ArrayList<>();
        for (String item : config.alchItems().split(",")) {
            String name = item.trim();
            if (!name.isEmpty()) {
                itemNames.add(name);
            }
        }

        for (String itemName : itemNames) {
            while (Rs2Inventory.hasItem(itemName)) {
                Rs2Magic.alch(itemName);
                if (!Rs2Player.waitForXpDrop(net.runelite.api.Skill.MAGIC, 10_000, false)) {
                    return;
                }
            }
        }
    }
}
