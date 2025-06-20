package net.runelite.client.plugins.microbot.bee.chaosaltar;

import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldArea;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2Pvp;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static net.runelite.api.ItemID.BURNING_AMULET5;
import static net.runelite.api.ItemID.DRAGON_BONES;
import static net.runelite.api.NpcID.CHAOS_FANATIC;
import static net.runelite.client.plugins.microbot.util.walker.Rs2Walker.*;


public class ChaosAltarScript extends Script {

    public static final WorldArea CHAOS_ALTAR_AREA = new WorldArea(2947, 3818, 11, 6, 0);
    public static final WorldArea CHAOS_ALTAR_FRONT_AREA = new WorldArea(2948, 3818, 5, 5, 0);
    public static final WorldPoint CHAOS_ALTAR_POINT = new WorldPoint(2949, 3820,0);
    public static final WorldPoint CHAOS_ALTAR_POINT_SOUTH = new WorldPoint(3014, 3820,0);

    private ChaosAltarConfig config;

    private State currentState = State.UNKNOWN;

    public static boolean test = false;
    public boolean run(ChaosAltarConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                Rs2Combat.disableAutoRetaliate();

                // Determine current state
                currentState = determineState();
                Microbot.log("Current state: " + currentState);

                // Execute state action
                switch (currentState) {
                    case BANK:
                        handleBanking();
                        break;
                    case TELEPORT_TO_WILDERNESS:
                        teleportToWilderness();
                        break;
                    case WALK_TO_ALTAR:
                        if (!CHAOS_ALTAR_FRONT_AREA.contains(Rs2Player.getWorldLocation())) {
                            Microbot.log("Cur 1 " + CHAOS_ALTAR_FRONT_AREA.contains(Rs2Player.getWorldLocation()));
                            walkTo(CHAOS_ALTAR_POINT, 3);
                            Microbot.log("Cur 2 " + CHAOS_ALTAR_FRONT_AREA.contains(Rs2Player.getWorldLocation()));
                        }
                        break;
                    case OFFER_BONES:
                        if (config.giveBonesFast()) {
                            offerBonesFast();
                        } else {
                            offerBones();
                        }
                        break;
                    case DIE_TO_NPC:
                        dieToNpc();
                        break;
                    default:
                        System.out.println("Unknown state. Resetting...");
                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private State determineState() {
        boolean inWilderness = Rs2Pvp.isInWilderness();
        boolean hasBones = Rs2Inventory.count(DRAGON_BONES) > 4;
        boolean hasAnyBones = Rs2Inventory.contains(DRAGON_BONES);
        boolean atAltar = isAtChaosAltar();

        if (!inWilderness && !hasBones) {
            return State.BANK;
        }
        if (!inWilderness && hasBones) {
            return State.TELEPORT_TO_WILDERNESS;
        }
        if (inWilderness && hasAnyBones && !atAltar) {
            return State.WALK_TO_ALTAR;
        }
        if (inWilderness && hasAnyBones && atAltar) {
            return State.OFFER_BONES;
        }
        if (inWilderness && !hasAnyBones) {
            return State.DIE_TO_NPC;
        }

        return State.UNKNOWN;
    }
    public boolean isAtChaosAltar() {
        for (TileObject obj : Rs2GameObject.getAll()) {
            if (obj.getId() == 411) {
                if (obj instanceof GameObject) {
                    GameObject gameObject = (GameObject) obj;
                    System.out.println("Found Chaos Altar GameObject at: " + gameObject.getWorldLocation());
                    if (Rs2GameObject.isReachable(gameObject)) {
                        //Microbot.log("Chaos Altar f");
                        return true;
                    } else {
                        System.out.println("Chaos Altar found but not reachable.");
                    }
                }
            }
        }
//        if(Rs2GameObject.exists(411)){
//            GameObject gameObject = Rs2GameObject.getAll(o->o.getId() == 411)
//                    .stream()
//                    .filter(o -> o instanceof GameObject)
//                    .map(o -> (GameObject) o)
//                    .findFirst()
//                    .orElse(null);
//
//            if (Rs2GameObject.isReachable(gameObject)) {
//                Microbot.log("Chaos Altar");
//                return true;
//            }
//        }
        return false;
    }


    private void dieToNpc() {
        //Microbot.log("Walking");
        //sleepUntil(() -> Rs2Npc.getNpc(CHAOS_FANATIC) != null, 2000);
        // Attack chaos fanatic to die
        if (Rs2Combat.inCombat() || Rs2Npc.attack("Chaos Fanatic")) {
            sleepUntil(() -> Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) == 0, 60000);
            sleepUntil(() -> !Rs2Pvp.isInWilderness(), 15000);
            sleep(1000,3000);
        }else{
            Rs2Walker.walkTo(2979, 3845,0,10);
            sleep(1500,3000);
        }
    }


    private void teleportToWilderness() {

        // Enable protect item if needed
//        if (!Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_ITEM)) {
//            System.out.println("Enabling Protect Item");
//            Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_ITEM, true);
//            sleep(500, 800);
//        }
        if (hasBurningAmulet() && !Rs2Pvp.isInWilderness()){
            walkTo(CHAOS_ALTAR_POINT_SOUTH,10);
            //Microbot.log("Fin");
        }
    }

    private void offerBones() {
        //Microbot.log("Offering bones s");

        if (!CHAOS_ALTAR_FRONT_AREA.contains(Rs2Player.getWorldLocation())) {
            Microbot.log("Cur 3 " + CHAOS_ALTAR_FRONT_AREA.contains(Rs2Player.getWorldLocation()));
            walkTo(CHAOS_ALTAR_POINT,3);
            Microbot.log("Cur 4 " + CHAOS_ALTAR_FRONT_AREA.contains(Rs2Player.getWorldLocation()));
        }

        //if (Rs2Player.isInCombat()) {offerBonesFast(); return;}

        if (CHAOS_ALTAR_FRONT_AREA.contains(Rs2Player.getWorldLocation()) && Rs2Inventory.contains(DRAGON_BONES) && isRunning()) {
            if (Rs2Inventory.slotContains(0,DRAGON_BONES)) {
                int randomSlot = 0;
                if (Rs2Inventory.count(DRAGON_BONES)>3){
                    randomSlot = Rs2Random.betweenInclusive(1,3);
                }
                Rs2Inventory.slotInteract(randomSlot, "use");
                sleep((int)Rs2Random.skewedRand(300,200,450,2));
                Rs2GameObject.interact(411);
                if (Rs2Random.dicePercentage(75)){
                    sleep((int)Rs2Random.skewedRand(300,200,450,2));
                }else{
                    Rs2Inventory.waitForInventoryChanges(2000);
                    //Microbot.log("cha");
                }
            }
        }
    }
    // Experimental, need to drag the inventory ( last slot ) next to the altar
    private void offerBonesFast() {
        Microbot.log("Offering bones f");

        if (!CHAOS_ALTAR_FRONT_AREA.contains(Rs2Player.getWorldLocation())) {
            walkTo(CHAOS_ALTAR_POINT,3);
        }
        if (Rs2Camera.getYaw() != 0){
            Rs2Camera.setYaw(0);
        }
        while (Rs2Inventory.contains(DRAGON_BONES)
                && CHAOS_ALTAR_FRONT_AREA.contains(Rs2Player.getWorldLocation())
                && isRunning()
                && Rs2GameObject.exists(411)) {
            Rs2Inventory.useLast(DRAGON_BONES);
            //sleep(150, 350);
            sleep((int)Rs2Random.skewedRand(250,150,350,2));
            Rs2GameObject.interact(411);
            //Rs2Player.waitForXpDrop(Skill.PRAYER);
            //sleep(150, 350);
            sleep((int)Rs2Random.skewedRand(250,150,350,2));
        }
    }


    private void handleBanking() {
        if(Rs2Inventory.contains(x-> x != null && x.getName().contains("Burning amulet"))){
            Rs2Inventory.wear("Burning amulet");
        }
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.walkToBankAndUseBank();
            Rs2Bank.openBank();
        } else {
            Rs2Bank.depositAll();

            if(!Rs2Bank.hasItem(DRAGON_BONES)) {
                Microbot.log("No Bones, shutting down");
                shutdown();
            }

            if(!Rs2Bank.hasBankItem("Burning Amulet")) {
                Microbot.log("No Burning Amulet, shutting down");
                shutdown();
            }

            // If amulet not equipped or in inventory
            if (!hasBurningAmulet()) {
                sleep(400);
                Rs2Bank.withdrawAndEquip("burning amulet");
                Rs2Inventory.waitForInventoryChanges(2000);
                sleep(1000,2000);
            }
            // If no bones in inventory, withdraw 28
            if (!Rs2Inventory.contains(DRAGON_BONES)) {
                Rs2Bank.withdrawAll(DRAGON_BONES);
                Rs2Inventory.waitForInventoryChanges(2000);
                sleep(1000,2000);
            }

            Rs2Bank.closeBank();
        }
    }
    public boolean hasBurningAmulet() {
        return Rs2Inventory.contains(x-> x != null && x.getName().contains("Burning amulet")) || Rs2Equipment.isWearing("burning amulet");
    }


    @Override
    public void shutdown() {
        super.shutdown();
    }
}
