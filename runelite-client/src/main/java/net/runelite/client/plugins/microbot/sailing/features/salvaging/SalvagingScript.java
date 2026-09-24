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
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ObjectID1;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.api.npc.Rs2NpcQueryable;
import net.runelite.client.plugins.microbot.api.npc.models.Rs2NpcModel;
import net.runelite.client.plugins.microbot.api.tileobject.Rs2TileObjectQueryable;
import net.runelite.client.plugins.microbot.api.tileobject.models.Rs2TileObjectModel;
import net.runelite.client.plugins.microbot.sailing.SailingConfig;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Singleton
public class SalvagingScript {

    private static final int SALVAGE_RANGE = 15;
    private static final int INVENTORY_THRESHOLD = 24;
    private static final int ACTION_TIMEOUT_MS = 5_000;
    private static final int DEPOSIT_TIMEOUT_MS = 20_000;
    private int occupiedCapacity = -1;
    private int totalCapacity = -1;

    private final EventBus eventBus;

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
        if (Rs2Widget.isWidgetVisible(InterfaceID.SailingBoatCargohold.CAPACITY_CONTAINER)) {
            occupiedCapacity = Integer.parseInt(
                    Rs2Widget.getWidget(InterfaceID.SailingBoatCargohold.OCCUPIEDSLOTS).getText()
            );

            totalCapacity = Integer.parseInt(
                    Rs2Widget.getWidget(InterfaceID.SailingBoatCargohold.CAPACITY).getText()
            );
        }
    }

    public void run(SailingConfig config) {
        Microbot.log("1");
        /// check cargo hold and start crystal extractor
        if (occupiedCapacity == -1 || totalCapacity == -1) {
            openCargoHold();
            closeCargoHold();
        }

        /// if animating, wait until it finishes animating
        if (Rs2Player.isAnimating()) {
            return;
        }
        /// if
        ///  determine if crewmate is animating
        if(nearestNpcAnimating())
        Microbot.log("2");
        //// if
        if ( occupiedCapacity >= totalCapacity ) {
            Microbot.log("Occupied Capacity: " + occupiedCapacity + " / " + totalCapacity);
            if (Rs2Inventory.count("Grimy")>0) {
                Rs2Inventory.interact("herb sack","Fill");
            }
            if(Rs2Inventory.count("salvage") > 0){
                sortSalvage();
            }
            if(Rs2Inventory.count("salvage") <= 0){
                dropConfiguredItems(config);
            }

        }
        Microbot.log("3");
        if (nearestActiveWreck()) {
            deployHook();
        }
        Microbot.log("4");
    }

    private boolean nearestActiveWreck() {
        //merchant ship active 60478 , inactive 60479
        Rs2TileObjectModel wreck = new Rs2TileObjectQueryable()
                .withId(60478)
                .nearest(15);

        return wreck != null;
    }

    private boolean nearestNpcAnimating() {
        List<Rs2NpcModel> npcs = new Rs2NpcQueryable()
                .withNames("Jolly Jim", "Cabin Boy Jenkins")
                .fromWorldView()
                .toList();

        return npcs.stream()
                .anyMatch(n -> n.getAnimation() != -1);
    }

    private boolean openCargoHold(){
        var cargoHold = new Rs2TileObjectQueryable()
                .withId(ObjectID1.SAILING_BOAT_CARGO_HOLD_ROSEWOOD_LARGE)
                .fromWorldView()
                .first();

        if (cargoHold == null){
            return false;
        }
        var actions = cargoHold.getObjectComposition().getActions();
        Microbot.log("Actions"+ actions);

        if (actions == null || !Arrays.stream(actions)
                .anyMatch(action -> "Open".equalsIgnoreCase(action))) {
            return false;
        }

        cargoHold.click("Open");
        sleepUntil(()->Rs2Widget.isWidgetVisible(InterfaceID.SailingBoatCargohold.CAPACITY_CONTAINER),10000);
        return Rs2Widget.isWidgetVisible(InterfaceID.SailingBoatCargohold.CAPACITY_CONTAINER);
    }

    private boolean closeCargoHold(){
        if (Rs2Widget.isWidgetVisible(InterfaceID.SailingBoatCargohold.CAPACITY_CONTAINER)){
            return Rs2Widget.clickWidget("Close", Optional.of(943),1,true);
        }
        return false;
    }

    private boolean sortSalvage() {
        if (Rs2Inventory.count("salvage") <= 0) {
            return false;
        }


        var station = new Rs2TileObjectQueryable()
                .withNameContains("salvaging station")
                .fromWorldView()
                .first();

        if (station == null) {
            return false;
        }

        station.click("Sort-salvage");
        sleepUntil(() -> Rs2Inventory.count("salvage") == 0, DEPOSIT_TIMEOUT_MS);
        return Rs2Inventory.count("salvage") == 0;

    }

    private boolean deployHook() {
        Rs2TileObjectModel hook = new Rs2TileObjectQueryable()
                .withId(ObjectID1.SALVAGING_HOOK_LARGE_RUNE_B)
                .fromWorldView()
                .first();

        if (hook == null) {
            return false;
        }

        hook.click("Deploy");
        sleepUntil(Rs2Player::isAnimating, ACTION_TIMEOUT_MS);
        return Rs2Player.isAnimating(5000);
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
