package net.runelite.client.plugins.microbot.qualityoflife.scripts;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.looter.enums.DefaultLooterStyle;
import net.runelite.client.plugins.microbot.qualityoflife.QoLConfig;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.poh.*;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldArea;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2Cannon;
import net.runelite.client.game.npcoverlay.HighlightedNpc;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.poh.PohTeleports;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.skills.slayer.Rs2Slayer;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.npchighlight.NpcIndicatorsPlugin;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SmartSlayer extends Script {


@Getter
@Setter
private static boolean completedSlayerTask = false;
private static String slayerMonster = null;


    public boolean run(QoLConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run() || !config.smartSlayer()) return;
                List<String> monsters = Rs2Slayer.getSlayerMonsters();
                Map<NPC, HighlightedNpc> highlightedNpcs =  net.runelite.client.plugins.npchighlight.NpcIndicatorsPlugin.getHighlightedNpcs();
//                NpcIndicatorsPlugin plugin = Microbot.getPlugin(NpcIndicatorsPlugin.class);
//                Map<NPC, HighlightedNpc> highlightedNpcs = plugin.getHighlightedNpcs();
                AtomicBoolean isNearSlayerMonster = new AtomicBoolean(false);
                if(Rs2Inventory.contains(false,"cannon base")) return;
                if (monsters != null) {
                    for (String monster : monsters) {
    //                        Rs2Npc.getNpcs(monster).forEach(npc -> {
    //                            if (!npc.isDead() && Rs2Player.getWorldLocation().distanceTo(npc.getWorldLocation()) <= 5) {
    //                                isNearSlayerMonster.set(true);
    //                                //int distance = Rs2Player.getWorldLocation().distanceTo(npc.getWorldLocation());
    //                                //Microbot.log("Nearby " + distance);
    //                            }
    //                        });
    //                        Optional.ofNullable(Rs2Npc.getNpcs(monster))
    //                            .orElse(Stream.empty())
    //                            .forEach(npc -> {
    //                                if (!npc.isDead() && Rs2Player.getWorldLocation().distanceTo(npc.getWorldLocation()) <= 5) {
    //                                    isNearSlayerMonster.set(true);
    //                                }
    //                            })
                        Optional.ofNullable(Rs2Npc.getNpcs(monster))
                            .orElse(Stream.empty())
                            .filter(npc -> npc.getName() != null && !npc.getName().toLowerCase().contains("superior"))
                            .forEach(npc -> {
                                if (npc == null) {
                                    Microbot.log("Npc is null for monster: " + monster);
                                    return;
                                }
                                if (npc.getWorldLocation() == null || Rs2Player.getWorldLocation() == null) {
                                    Microbot.log("Null location: NPC=" + npc.getName());
                                    return;
                                }

                                if (!npc.isDead() && Rs2Player.getWorldLocation().distanceTo(npc.getWorldLocation()) <= 8) {
                                    slayerMonster = monster;
                                    isNearSlayerMonster.set(true);
                                }
                            });
                    }
                }
                if (!highlightedNpcs.isEmpty()) {
                    for (NPC npc : highlightedNpcs.keySet()) {
                        if (npc != null && !npc.isDead()) {
                            if (npc.getWorldLocation() != null && Rs2Player.getWorldLocation() != null) {
                                if (Rs2Player.getWorldLocation().distanceTo(npc.getWorldLocation()) <= 8) {
                                    //Microbot.log("Highlighted slayer monster is " + Rs2Player.getWorldLocation().distanceTo(npc.getWorldLocation()) + " tiles far");
                                    slayerMonster = npc.getName();
                                    isNearSlayerMonster.set(true);
                                    break;
                                }
                            }
                        }
                    }
                }
                if(Rs2Slayer.hasSlayerTask() && isNearSlayerMonster.get()){
                    Rs2Combat.enableAutoRetialiate();
                    Rs2ItemModel currentGlove = Rs2Equipment.get(EquipmentInventorySlot.GLOVES);
                    if(currentGlove == null){
                        if(Rs2Inventory.contains(21183)) {
                            sleep(Rs2Random.skewedRandAuto(700));
                            Rs2Inventory.interact(21183, "Wear");
                        }
                        if(Rs2Inventory.contains(21177)) {
                            sleep(Rs2Random.skewedRandAuto(700));
                            Rs2Inventory.interact(21177, "Wear");
                        }
                    }

                    if (config.autoLootOnValue()) {
                        LootingParameters valueParams = new LootingParameters(
                                config.autoLootValueAmount(),
                                Integer.MAX_VALUE,
                                15,
                                1,
                                2,
                                true,
                                true
                        );
                        Rs2GroundItem.lootItemBasedOnValue(valueParams);
                        sleep(1000,2000);
                    }

                    while (isRunning()
                            && config.tagMultipleMonster()
                            && Rs2Npc.getNpcsForPlayer(slayerMonster).size() < 3
                            && Rs2Player.isInMulti())
                    {
                        // For Tag Multiple Monster
                        List<Rs2NpcModel> validNpc = Rs2Npc.getAttackableNpcs(true)
                                .filter(npc -> npc.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) <= 8)
                                .filter(npc -> slayerMonster.contains(npc.getName()))
                                .collect(Collectors.toList());

                        // Pick a random NPC safely
                        int needed = 3 - Rs2Npc.getNpcsForPlayer(slayerMonster).size();
                        if (validNpc.isEmpty() || validNpc.size() < needed) {
                            Microbot.log("Not enough valid NPCs: " + validNpc.size() + " (need " + needed + ")");
                            break; // stop the loop safely
                        }

                        Microbot.log("ValidNpc: " + validNpc.size() + " Current interacting with me: " + Rs2Npc.getNpcsForPlayer(slayerMonster).size());

                        Rs2NpcModel selectedNpc = validNpc.get(Rs2Random.betweenInclusive(0, validNpc.size() - 1));
                        Microbot.log("Selected NPC: " + selectedNpc.getName() + " null " + (selectedNpc != null) + " Dead " + selectedNpc.isDead() + " interact " + selectedNpc.getInteracting());
                        // Attack only if the NPC isn’t already engaged
                        if (selectedNpc != null && !selectedNpc.isDead() && selectedNpc.getInteracting() == null) {
                            Rs2Npc.attackInMulti(selectedNpc);
                            sleepUntil(() -> {
                                var interacting = selectedNpc.getInteracting();
                                return interacting != null && interacting.equals(Microbot.getClient().getLocalPlayer());
                            }, 5000);
                            if (selectedNpc.getInteracting() != null ) {
                                Microbot.log("selectedNpc2: " + selectedNpc.getInteracting() + " Interacting With: " + selectedNpc.getInteracting().equals(Microbot.getClient().getLocalPlayer()));
                            }
                        } else {
                            // Remove invalid NPCs to avoid infinite looping on dead or already-engaged targets
                            validNpc.remove(selectedNpc);
                        }
                    }

                    if(!Rs2Combat.inCombat()){
                        int waited = 0;
                        int timeout = Rs2Random.between(3000,8000);
                        System.out.println(timeout);
                        while (waited < timeout) {
                            if (Rs2Combat.inCombat()) {
                                break;
                            }
                            sleep(250);
                            waited += 250;
                        }
                        if(!Rs2Combat.inCombat()){
                            if (monsters != null && !monsters.isEmpty()) {
                                Rs2Npc.attack(monsters);
                            } else if (!highlightedNpcs.isEmpty()) {
                                highlightedNpcs.keySet().stream()
                                        .filter(npc -> npc != null && !npc.isDead() && npc.getWorldLocation() != null)
                                        .min(Comparator.comparingInt(npc ->
                                                Rs2Player.getWorldLocation().distanceTo(npc.getWorldLocation())))
                                        .ifPresent(npc -> Rs2Npc.attack(new Rs2NpcModel(npc)));
                            } else {
                                Microbot.log("No slayer monsters found.");
                            }
                        }
                    }
                        if (Rs2Combat.inCombat()){
                            if (Rs2Player.drinkGoadingPotion()){
                                 Rs2Player.waitForAnimation();
                           }
                        }
                }
                if (completedSlayerTask){
    //            //if(!Rs2Bank.walkToBankAndUseBank()) return;
                    Microbot.log("Slayer completed");
                    sleep(5000,10000);
                    while (Rs2GameObject.exists(6) || Rs2GameObject.exists(43027) && isRunning()){
                        if(Rs2Inventory.emptySlotCount()>=4) {
                            if (Rs2GameObject.interact(6, "Pick-up") || Rs2GameObject.interact(43027, "Pick-up")) {
                                sleepUntil(() -> !Rs2GameObject.exists(6) || !Rs2GameObject.exists(43027));
                            }
                            sleep(500, 1000);
                        }else{
                            shutdown();
                        }
                    }
                    if (Rs2Inventory.contains(8013)) {
                        while (Rs2Inventory.contains(8013) && !PohTeleports.isInHouse() && isRunning()) {
                            Rs2Inventory.interact(8013, "Break");
                            sleepUntil(PohTeleports::isInHouse,10000);
                            sleep(500, 1000);
                        }
                    } else if (Rs2Inventory.contains(9790)){
                        while (Rs2Inventory.contains(9790) && !PohTeleports.isInHouse() && isRunning()) {
                            Rs2Inventory.interact(9790, "Tele to POH");
                            sleepUntil(PohTeleports::isInHouse,10000);
                            sleep(500, 1000);
                        }
                    } else if (Rs2Inventory.contains(13393)) {
                        Rs2Bank.walkToBank();
                        sleepUntil(() ->Rs2Bank.isNearBank(5));
                    }
                    sleep(1000,3000);
                    Rs2Tab.switchTo(InterfaceTab.PRAYER);
                    Rs2Prayer.disableAllPrayers(true);
                    Microbot.getConfigManager().setConfiguration("npcindicators", "npcToHighlight", "");
                    completedSlayerTask = false;
                }
            } catch(Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    return true;
    }
}
