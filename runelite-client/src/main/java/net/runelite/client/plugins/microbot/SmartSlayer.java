package net.runelite.client.plugins.microbot.qualityoflife.scripts;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.qualityoflife.QoLConfig;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2Cannon;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.poh.PohTeleports;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.slayer.Rs2Slayer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class SmartSlayer extends Script {


@Getter
@Setter
private static boolean completedSlayerTask = false;


    public boolean run(QoLConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run() || !config.smartSlayer()) return;
                List<String> monsters = Rs2Slayer.getSlayerMonsters();
                AtomicBoolean isNearSlayerMonster = new AtomicBoolean(false);
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
//                            });
                        Optional.ofNullable(Rs2Npc.getNpcs(monster))
                                .orElse(Stream.empty())
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
                                        isNearSlayerMonster.set(true);
                                    }
                                });
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
                            } else {
                                Microbot.log("No slayer monsters found.");
                            }
                        }
                    }
                }
                if (completedSlayerTask){
//            //if(!Rs2Bank.walkToBankAndUseBank()) return;
                    Microbot.log("Slayer completed");
                    sleep(5000,10000);
                    if(Rs2GameObject.exists(6)){
                        Rs2GameObject.interact(6,"Pick-up");
                        sleepUntil(()-> !Rs2GameObject.exists(6));
                        sleep(500,1000);
                    }
                    if(Rs2Inventory.contains(8013)) {
                        Rs2Inventory.interact(8013, "break");
                        sleepUntil(PohTeleports::isInHouse);
                    }else if (Rs2Inventory.contains(13393)) {
                        Rs2Inventory.interact(13393,"teleport",131076);
                        Rs2Bank.walkToBank();
                        sleepUntil(() ->Rs2Bank.isNearBank(5));
                    }
                    sleep(1000,3000);
                    Rs2Prayer.disableAllPrayers();
                    completedSlayerTask = false;
                }
            } catch(Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
}
