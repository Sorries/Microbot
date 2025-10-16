package net.runelite.client.plugins.microbot.example;

import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.storm.plugins.PlayerMonitor.PlayerMonitorPlugin;
import net.runelite.client.plugins.microbot.util.Rs2InventorySetup;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.grounditem.InteractModel;

import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.grounditems.GroundItem;
import net.runelite.client.plugins.grounditems.GroundItemsPlugin;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject.nameMatches;

public class ExampleScript extends Script {

    public static boolean test = false;

    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
//                Microbot.stopPlugin(Microbot.getPlugin(PluginDisablerPlugin.class.getName()));
//                System.out.println(""+ Microbot.getPlugin(PluginDisablerPlugin.class.getName()));
//                String result = String.valueOf(SmartSlayer.isCompletedSlayerTask());
//                System.out.println(result);
//                if (SmartSlayer.getCompletedSlayerTask()) {
//                    Microbot.log("Slayer task is complete!");
//                }else{
//                    Microbot.log("Slayer task is not complete!");
//                }
//                for (Rs2ItemModel item : Rs2Inventory.items().stream()
//                        .filter(x -> {
//                            String[] actions = x.getInventoryActions();
//                            return actions != null && (Arrays.asList(actions).contains("Wield") || Arrays.asList(actions).contains("Wear"));
//                        })
//                        .collect(Collectors.toList())) {
//
//                    if (Arrays.asList(item.getInventoryActions()).contains("Wield")) {
//                        Rs2Inventory.interact(item, "Wield");
//                    } else if (Arrays.asList(item.getInventoryActions()).contains("Wear")) {
//                        Rs2Inventory.interact(item, "Wear");
//                    }
//
//                    // Debug (optional)
//                    System.out.println("Equipped: " + item.getName()+ " Item Actions: " + Arrays.toString(item.getInventoryActions()));
//                }

//                for (Rs2ItemModel item : Rs2Inventory.items().stream()
//                        .filter(x -> Arrays.asList(x.getInventoryActions()).contains("Wear"))
//                        .toList()) {
//                    Rs2Inventory.interact(item, "Wear");
//                    // Optional: Debug output
//                    // System.out.println("Wearing: " + item.getName());
//                }

//                System.out.println("Object"+ Rs2GameObject.getAll());
//                Rs2GameObject.getGameObject("Altar",true).getId();

//                List<Integer> altar = Rs2GameObject.getAll()
//                        .stream()
//                        .filter(nameMatches("Altar", false))
//                        .map(x->x.getId())
//                        .collect(Collectors.toList());
//                System.out.println("Altar: " + altar);
//                long endTime = System.currentTimeMillis();
//                long totalTime = endTime - startTime;
//                System.out.println("Total time for loop " + totalTime);
//
//                            // IDs of the shaking boxes
//            int[] shakingBoxIds = {
//                    ObjectID.SHAKING_BOX_9384,
//                    ObjectID.SHAKING_BOX_9383,
//                    ObjectID.SHAKING_BOX_9382
//            };
//
//// Collect all shaking boxes into one list
//                List<Rs2ObjectModel> shakingBoxes = new ArrayList<>();
//
//                for (int id : shakingBoxIds) {
//                    List<GameObject> gameObjects = Rs2GameObject.getGameObjects(obj -> obj.getId() == id);
//                    if (gameObjects != null) {
//                        for (GameObject gameObject : gameObjects) {
//                            if (gameObject != null) {
//                                Tile tile = Microbot.getClient()
//                                        .getScene()
//                                        .getTiles()[gameObject.getPlane()][gameObject.getSceneX()][gameObject.getSceneY()];
//                                shakingBoxes.add(new Rs2ObjectModel(gameObject, tile));
//                            }
//                        }
//                    }
//                }
//
//// Sort by earliest spawn tick (first-come-first-serve)
//            shakingBoxes.sort(Comparator.comparingInt(Rs2ObjectModel::getCreationTick));
//
////// Try to interact with the first one
////            if (!shakingBoxes.isEmpty()) {
////                Rs2ObjectModel firstBox = shakingBoxes.get(0);
////                System.out.println("firstBox"+firstBox);
//////                if (Rs2GameObject.interact(firstBox.getId(), "reset", 4)) {
//////                    return;
//////                }
////            }
//                if(Microbot.isPluginEnabled(ShortestPathPlugin.class)){
//                    Microbot.log("active");
//                    var plugin = Microbot.getPlugin(ShortestPathPlugin.class);
//                    if (plugin != null) {
//                        Microbot.log("shutting down");
//                        plugin.getShortestPathScript().setTriggerWalker(null);
//                    } else {
//                        Microbot.log("ShortestPathScript is not running.");
//                    }
//                }

//                private Bars resolveActiveBar() {
//                    Bars configured = config.getBars();
//                    int level = Rs2Player.getBoostedSkillLevel(Skill.SMITHING);
//
//                    Microbot.log("Resolve: start (configured=" + configured + ", lvl=" + level + ")");
//
//
//                    if (configured != null && level >= configured.getMinSmithingLevel()) {
//                        int invCntConfigured = Rs2Inventory.count(configured.getPrimaryOre());
//                        Microbot.log("Resolve: inv " + configured + " primary=" + invCntConfigured + "/27");
//                        if (invCntConfigured >= 27) {
//                            Microbot.log("Resolve: selecting configured from inventory: " + configured);
//                            return configured;
//                        }
//                    }
//
//                    if (this.activeBar != null && level >= this.activeBar.getMinSmithingLevel()) {
//                        int invCntActive = Rs2Inventory.count(this.activeBar.getPrimaryOre());
//                        Microbot.log("Resolve: inv " + this.activeBar + " primary=" + invCntActive + "/27");
//                        if (invCntActive >= 27) {
//                            Microbot.log("Resolve: finishing previous active from inventory: " + this.activeBar);
//                            return this.activeBar;
//                        }
//                    }
//
//                    Bars[] priority = new Bars[] { Bars.RUNITE_BAR, Bars.ADAMANTITE_BAR, Bars.MITHRIL_BAR, Bars.STEEL_BAR };
//
//                    for (Bars b : priority) {
//                        if (b == configured || b == this.activeBar) continue;
//                        int invCnt = Rs2Inventory.count(b.getPrimaryOre());
//                        if (level >= b.getMinSmithingLevel() && invCnt >= 27) {
//                            Microbot.log("Resolve: selecting from inventory by priority: " + b + " (primary=" + invCnt + ")");
//                            return b;
//                        }
//                    }
//
//                    if (configured == Bars.GOLD_BAR) {
//                        if (level >= configured.getMinSmithingLevel() && hasRequiredOresForBar(configured)) {
//                            Microbot.log("Resolve: selecting configured via bank: " + configured);
//                            return configured;
//                        }
//                        return null; // Gold does not fallback to other bars
//                    }
//
//                    // Try configured bar first (bank-based) with 27/54 threshold when applicable
//                    if (level >= configured.getMinSmithingLevel()) {
//                        if (!Rs2Bank.isOpen()) {
//                            Rs2Bank.openBank();
//                            sleepUntil(Rs2Bank::isOpen, 2000);
//                        }
//                        int primaryCount = Rs2Bank.count(configured.getPrimaryOre());
//                        Integer sec = configured.getSecondaryOre();
//                        boolean requires54Coal = sec != null && sec == ItemID.COAL;
//                        String secStr = (sec == null) ? "N/A" : String.valueOf(Rs2Bank.count(sec));
//                        Microbot.log("Resolve: bank " + configured + " primary=" + primaryCount + ", secondary=" + secStr + (requires54Coal ? " (need>=54 coal)" : (sec == null ? "" : " (need>=27)")));
//
//                        if (hasAtLeast27PrimaryAnd54SecondaryInBank(configured)) {
//                            Microbot.log("Resolve: selecting configured via bank: " + configured);
//                            return configured;
//                        } else {
//                            Microbot.log("Resolve: configured bank threshold not met, trying fallbacks");
//                        }
//                    }

//                LocalPoint.fromWorld(Microbot.getClient().getTopLevelWorldView(), new WorldPoint(1,1,1));
//                Microbot.getClient().getLocalPlayer();
//                Rs2Player.getPlayerEquipmentIds(new Rs2PlayerModel(Microbot.getClient().getLocalPlayer()));
//                Rs2Npc.getNpcsForPlayer();
//                RS2Item item = Arrays.stream(Rs2GroundItem.getAll(255))
//                        .filter(rs2Item -> rs2Item.getItem().getId() == 7946)
//                        .findFirst().orElse(null);
//                Rs2GroundItem.interact(new InteractModel(item.getTileItem().getId(), item.getTile().getWorldLocation(), item.getItem().getName()), "Take",true);
//                Widget[] parentWidget = Rs2Widget.getWidget(25362432).getChildren(); //0x0183_0000
//                Widget slot0 = getWidgetByPackedId(InterfaceID.Wornitems.SLOT0);
//                Microbot.log("slot0 "+ slot0);
//                Widget slot1 = Rs2Widget.getWidget(0x0183_000f);
//                Microbot.log("slot1 "+ slot1);
//                if(Rs2Equipment.isWearing()) {
//                    Rs2Equipment.unEquip(e -> true);
//                }
                LootingParameters nameParams = new LootingParameters(
                        15,
                        1,
                        1,
                        2,
                        false,
                        true,
                        "Blood rune","Araxyte venom sack"
                );
                Rs2GroundItem.lootItemsBasedOnNames(nameParams);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
    public static Widget getWidgetByPackedId(int packedId) {
        int parent = (packedId >> 16) & 0xFFFF;
        int child = packedId & 0xFFFF;
        return Rs2Widget.getWidget(parent, child);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}