package net.runelite.client.plugins.microbot.sailing.features.salvaging;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ObjectID1;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.api.npc.Rs2NpcQueryable;
import net.runelite.client.plugins.microbot.api.npc.models.Rs2NpcModel;
import net.runelite.client.plugins.microbot.api.tileobject.Rs2TileObjectQueryable;
import net.runelite.client.plugins.microbot.api.tileobject.models.Rs2TileObjectModel;
import net.runelite.client.plugins.microbot.sailing.SailingConfig;
import net.runelite.client.plugins.microbot.util.inventory.InteractOrder;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class SalvagingScript extends Script {

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

        /// check cargo hold from opening widget
        if (occupiedCapacity == -1 || totalCapacity == -1) {
            Microbot.log("Opening Cargo Hold to check capacity");
            openCargoHold();
            Microbot.log("Occupied Capacity: " + occupiedCapacity + " / " + totalCapacity);
            sleep(2000,10000);
            closeCargoHold();
        }
        /// check crystal extractor status
        if (!crystalExtractorStatus()){
            sleep(1000,5000);
            activateCrystalExtractor();
        }
        /// if animating, wait until it finishes animating
        if (Rs2Player.isAnimating()) {
            return;
        }
        /// if crystal extractor can be harvested
        if(crystalExtractorStatus()){
            sleep(1000,5000);
            harvestCrystalExtractor();
        }

        ///  determine if cargo hold is full
        if(!nearestNpcAnimating() && nearestActiveWreck()){
            occupiedCapacity = totalCapacity;
        }

        // Todo : add cargo hold withdraw
        if ( occupiedCapacity >= totalCapacity ) {
            Microbot.log("Occupied Capacity2 : " + occupiedCapacity + " / " + totalCapacity);
            openCargoHold();

            closeCargoHold();
        }

        ///  sorting salvage
        if(Rs2Inventory.isFull()){
            salvaging(config);
            return;
        }

        /// deploying hook
        if (nearestActiveWreck() && !Rs2Inventory.isFull()) {
            sleep(2000,10000);
            deployHook();
        }

    }

    private boolean nearestActiveWreck() {
        //merchant ship active 60478 , inactive 60479
        Rs2TileObjectModel wreck = new Rs2TileObjectQueryable()
                .withId(ObjectID1.SAILING_MERCHANT_SHIPWRECK)
                .nearestOnClientThread(15);

        return wreck != null;
    }

    private boolean crystalExtractorStatus(){
        Rs2TileObjectModel crystalInactive = new Rs2TileObjectQueryable()
                .fromWorldView()
                .withId(ObjectID1.SAILING_CRYSTAL_EXTRACTOR_DEACTIVATED)
                .firstOnClientThread();

        Rs2TileObjectModel crystalActive = new Rs2TileObjectQueryable()
                .fromWorldView()
                .withId(ObjectID1.SAILING_CRYSTAL_EXTRACTOR_ACTIVATED)
                .firstOnClientThread();

        if (crystalActive != null ) {
            return true;}
        else if (crystalInactive != null){
            return false;}
        return true;
    }

    private void activateCrystalExtractor () {
        Rs2TileObjectModel crystalInactive = new Rs2TileObjectQueryable()
                .fromWorldView()
                .withId(ObjectID1.SAILING_CRYSTAL_EXTRACTOR_DEACTIVATED)
                .firstOnClientThread();

        if (crystalInactive != null) {
            Microbot.log("Activating Crystal Extractor");
            crystalInactive.click("Activate");
        }
    }

    private void harvestCrystalExtractor(){
        Rs2TileObjectModel crystalActive = new Rs2TileObjectQueryable()
                .fromWorldView()
                .withId(ObjectID1.SAILING_CRYSTAL_EXTRACTOR_ACTIVATED)
                .firstOnClientThread();
        if (crystalActive != null) {
            if (crystalActive.getTileObject() instanceof GameObject) {
                GameObject object = (GameObject) crystalActive.getTileObject();

                if (object.getRenderable() instanceof DynamicObject) {
                    DynamicObject dynamicObject = (DynamicObject) object.getRenderable();

                    Animation animation = dynamicObject.getAnimation();

                    if (animation != null &&
                            animation.getId() == AnimationID.SAILING_BOATS_CRYSTAL_EXTRACTOR_KANDARIN_EXTRACTED_01) {
                        Microbot.log("Animation ID is " + animation.getId());
                        Microbot.log("Harvesting Crystal Extractor");
                        crystalActive.click("Harvest");
                    }
                }
            }
        }
    }

    private boolean nearestNpcAnimating() {
        List<Rs2NpcModel> npcs = new Rs2NpcQueryable()
                .fromWorldView()
                .withNames("Jolly Jim", "Cabin Boy Jenkins")
                .toList();

        return npcs.stream()
                .anyMatch(n -> n.getAnimation() != -1);
    }

    private boolean openCargoHold(){
        var cargoHold = new Rs2TileObjectQueryable()
                .fromWorldView()
                .withId(ObjectID1.SAILING_BOAT_CARGO_HOLD_ROSEWOOD_LARGE)
                .firstOnClientThread();

        if (cargoHold == null){
            return false;
        }
        var actions = cargoHold.getObjectComposition().getActions();
        Microbot.log("Actions"+ Arrays.toString(actions));

        if (actions == null || !Arrays.stream(actions)
                .anyMatch(action -> "Open".equalsIgnoreCase(action))) {
            return false;
        }
        Microbot.log("Open cargo hold");
        cargoHold.click("Open");
        sleepUntil(()->Rs2Widget.isWidgetVisible(InterfaceID.SailingBoatCargohold.CAPACITY_CONTAINER),10000);
        return Rs2Widget.isWidgetVisible(InterfaceID.SailingBoatCargohold.CAPACITY_CONTAINER);
    }

    private boolean closeCargoHold(){
        if (Rs2Widget.isWidgetVisible(InterfaceID.SailingBoatCargohold.CAPACITY_CONTAINER)){
            Microbot.log("Close cargo hold");
            return Rs2Widget.clickWidget("Close", Optional.of(943),1,true);
        }
        return false;
    }

    private boolean withdrawCargoHold(){
        if (Rs2Widget.isWidgetVisible(InterfaceID.SailingBoatCargohold.CAPACITY_CONTAINER)) {
            Widget selectAll = Rs2Widget.getWidget(InterfaceID.SailingBoatCargohold.ALL);
            if (selectAll != null){
            }


            //Widget widget = Rs2Widget.getWidget(InterfaceID.SailingBoatCargohold.ITEMS);
            //if (widget != null) {
            //}
            Microbot.log("Withdraw cargo hold");
            //
            Widget widget = Rs2Widget.getWidget(InterfaceID.SailingBoatCargohold.ITEMS);

            if (widget != null) {
                Widget[] children = widget.getDynamicChildren();

                if (children != null) {
                    for (Widget child : children) {
                        if (child == null) {
                            continue;
                        }

                        String name = child.getName();

                        if (name == null || name.isBlank()) {
                            continue;
                        }

                        String[] actions = child.getActions();

                        if (actions == null) {
                            continue;
                        }

                        if (name.toLowerCase().contains("salvage") && Arrays.stream(actions)
                                .anyMatch(action -> action != null && action.equalsIgnoreCase("withdraw-all"))) {
                            Rs2Widget.clickWidgetFast(child,2,1,name,"Withdraw-All");

                            Microbot.log(
                                    "ID=" + child.getId() +
                                            " itemId=" + child.getItemId() +
                                            " text=" + name +
                                            " actions = " + Arrays.toString(actions)
                            );
                        }
                    }
                }
            }
            //
        }
        return false;
    }

    private void salvaging(SailingConfig config){
        if(Rs2Inventory.count("salvage") > 0){
            Microbot.log("Sorting salvage 1 " + Rs2Inventory.count("salvage"));
            sleep(1000,5000);
            sortSalvage();
        }
        if (Rs2Inventory.count("grimy")>0) {
            Microbot.log("Filling herb sack" + Rs2Inventory.count("grimy"));
            sleep(1000,5000);
            Rs2Inventory.interact("herb sack","Fill");
            Rs2Inventory.waitForInventoryChanges(500);
        }
        if(Rs2Inventory.count("salvage") <= 0){
            Microbot.log("Dropping salvage "+ Rs2Inventory.count("salvage"));
            sleep(2000,10000);
            dropItems(config);
        }
    }

    private boolean sortSalvage() {
        if (Rs2Inventory.count("salvage") <= 0) {
            return false;
        }

        var station = new Rs2TileObjectQueryable()
                .fromWorldView()
                .withNameContains("salvaging station")
                .firstOnClientThread();


        if (station == null) {
            return false;
        }
        Microbot.log("Sorting salvage 2");
        station.click("Sort-salvage");
        sleepUntil(() -> Rs2Inventory.count("salvage") == 0, 60000);
        return Rs2Inventory.count("salvage") == 0;

    }

    private boolean deployHook() {
        Rs2TileObjectModel hook = new Rs2TileObjectQueryable()
                .withId(ObjectID1.SALVAGING_HOOK_LARGE_RUNE_B)
                .fromWorldView()
                .firstOnClientThread();

        if (hook == null) {
            return false;
        }

        hook.click("Deploy");
        sleepUntil(Rs2Player::isAnimating);
        return Rs2Player.isAnimating(5000);
    }

    private void dropItems(SailingConfig config) {
        String configuredItems = config.dropItems();
        InteractOrder dropOrder = InteractOrder.EFFICIENT_ROW;

        if (configuredItems == null || configuredItems.isBlank()) {
            return;
        }

        String[] itemNames = Arrays.stream(configuredItems.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toArray(String[]::new);

        if (itemNames.length > 0) {
            Rs2Inventory.dropAll(
                    Rs2ItemModel.matches(false, itemNames),
                    dropOrder
            );
        }
    }
    @Override
    public void shutdown() {
        occupiedCapacity = -1;
        totalCapacity = -1;
        super.shutdown();
    }
}
