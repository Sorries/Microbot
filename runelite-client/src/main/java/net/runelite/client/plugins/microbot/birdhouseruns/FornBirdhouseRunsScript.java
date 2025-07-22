package net.runelite.client.plugins.microbot.birdhouseruns;

import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.Notifier;
import net.runelite.client.config.Notification;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import javax.inject.Inject;
import java.awt.*;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.birdhouseruns.FornBirdhouseRunsInfo.*;

public class FornBirdhouseRunsScript extends Script {
    private static final WorldPoint birdhouseLocation1 = new WorldPoint(3763, 3755, 0);
    private static final WorldPoint birdhouseLocation2 = new WorldPoint(3768, 3761, 0);
    private static final WorldPoint birdhouseLocation3 = new WorldPoint(3677, 3882, 0);
    private static final WorldPoint birdhouseLocation4 = new WorldPoint(3679, 3815, 0);
    public static double version = 1.1;
    @Inject
    private Notifier notifier;


    public boolean run(FornBirdhouseRunsConfig config) {
        Microbot.enableAutoRunOn = true;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                switch (botStatus) {
                    case GEARING:
                        if (Rs2Bank.openBank()) {
                            if (Rs2Inventory.getEmptySlots() <= 24 ){
                                Rs2Bank.depositAll();
                            }
                            if (config.GRACEFUL()) {
                                Rs2Bank.depositEquipment();
                                equipGraceful();
                            }
                            Rs2Bank.withdrawItem("Digsite Pendant");
                            if (config.TELEPORT()) {
                                Rs2Bank.withdrawOne(ItemID.LAW_RUNE);
                                Rs2Bank.withdrawOne(ItemID.FIRE_RUNE);
                                Rs2Bank.withdrawX(ItemID.AIR_RUNE, 3);
                            }
                            Rs2Bank.withdrawOne(ItemID.HAMMER);
                            Rs2Bank.withdrawOne(ItemID.CHISEL);
                            Rs2Bank.withdrawX(selectedLogs, 4);
                            Rs2Bank.withdrawX(selectedSeed, seedAmount * 4);
                            Rs2Inventory.waitForInventoryChanges(500);
                            while(Rs2Bank.isOpen()){
                                Rs2Bank.closeBank();
                                sleepUntil(()->!Rs2Bank.isOpen());
                            }
                            botStatus = states.TELEPORTING;
                        }
                        break;
                    case TELEPORTING:
                        int digsite = Rs2Inventory.get("Digsite Pendant").getId();
                        Rs2Inventory.interact(digsite,"rub",131078);
                        sleep(3000, 4000);
                        botStatus = states.VERDANT_TELEPORT;
                        break;
                    case VERDANT_TELEPORT:
                        if (interactWithObject(30920)) {
                            if (Rs2Widget.clickWidget(39845895)) {
                                sleep(3000, 4000);
                                botStatus = states.DISMANTLE_HOUSE_1;
                            }
                        }
                        break;
                    case DISMANTLE_HOUSE_1:
                        dismantleAndRebuildBirdhouse(30568, states.SEED_HOUSE_1);
                        break;
                    case SEED_HOUSE_1:
                        seedHouse(birdhouseLocation1, states.DISMANTLE_HOUSE_2);
                        break;
                    case DISMANTLE_HOUSE_2:
                        dismantleAndRebuildBirdhouse(30567, states.SEED_HOUSE_2);
                        break;
                    case SEED_HOUSE_2:
                        seedHouse(birdhouseLocation2, states.MUSHROOM_TELEPORT);
                        break;
                    case MUSHROOM_TELEPORT:
                        if (interactWithObject(30924)) {
                            if (Rs2Widget.clickWidget(39845903)) {
                                sleep(2000, 3000);
                                botStatus = states.DISMANTLE_HOUSE_3;
                            }
                        }
                        break;
                    case DISMANTLE_HOUSE_3:
                        dismantleAndRebuildBirdhouse(30565, states.SEED_HOUSE_3);
                        break;
                    case SEED_HOUSE_3:
                        seedHouse(birdhouseLocation3, states.DISMANTLE_HOUSE_4);
                        break;
                    case DISMANTLE_HOUSE_4:
                        Rs2Walker.walkTo(new WorldPoint(3680, 3815, 0));
                        dismantleAndRebuildBirdhouse(30566, states.SEED_HOUSE_4);
                        break;
                    case SEED_HOUSE_4:
                        seedHouse(birdhouseLocation4, states.FINISHING);
                        break;
                    case FINISHING:
                        if (config.TELEPORT()) {
                            Rs2Magic.cast(MagicAction.VARROCK_TELEPORT);
                            sleep(2500);
                        }
//                        emptyNests();
                        botStatus = states.FINISHED;
                        break;
                    case FINISHED:
//                        Rs2Walker.setTarget(null);
                        sleep(750,1250);
                        if (config.TELEPORT()) {
                            if (!Rs2Bank.isOpen()) {
                                Rs2Bank.walkToBankAndUseBank(BankLocation.GRAND_EXCHANGE);
                                return;
                            }
                        }
                        notifier.notify(Notification.ON, "Birdhouse run is finished.");
                        super.shutdown();
                        break;

                }

            } catch (Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private void emptyNests() {
        do {
            Rs2Inventory.interact(ItemID.BIRD_NEST, "search");
            sleep(1000);
        }
        while (Rs2Inventory.contains(ItemID.BIRD_NEST));

        do {
            Rs2Inventory.interact(ItemID.BIRD_NEST_5071, "search");
            sleep(1000);
        }
        while (Rs2Inventory.contains(ItemID.BIRD_NEST_5071));

        do {
            Rs2Inventory.interact(ItemID.BIRD_NEST_5072, "search");
            sleep(1000);
        }
        while (Rs2Inventory.contains(ItemID.BIRD_NEST_5072));

        do {
            Rs2Inventory.interact(ItemID.BIRD_NEST_5073, "search");
            sleep(1000);
        }
        while (Rs2Inventory.contains(ItemID.BIRD_NEST_5073));

        do {
            Rs2Inventory.interact(ItemID.BIRD_NEST_5074, "search");
            sleep(1000);
        }
        while (Rs2Inventory.contains(ItemID.BIRD_NEST_5074));

        do {
            Rs2Inventory.interact(ItemID.BIRD_NEST_22798, "search");
            sleep(1000);
        }
        while (Rs2Inventory.contains(ItemID.BIRD_NEST_22798));

        do {
            Rs2Inventory.interact(ItemID.BIRD_NEST_22800, "search");
            sleep(1000);
        }
        while (Rs2Inventory.contains(ItemID.BIRD_NEST_22800));
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    private void checkBeforeWithdrawAndEquip(String itemName) {
        if (!Rs2Equipment.isWearing(itemName)) {
            Rs2Bank.withdrawAndEquip(itemName);
        }
    }

    private boolean interactWithObject(int objectId) {
        Rs2GameObject.interact(objectId);
        sleepUntil(Rs2Player::isInteracting);
        sleepUntil(() -> !Rs2Player.isInteracting());
        return true;
    }

    private void equipGraceful() {
        checkBeforeWithdrawAndEquip("GRACEFUL HOOD");
        checkBeforeWithdrawAndEquip("GRACEFUL CAPE");
        checkBeforeWithdrawAndEquip("GRACEFUL BOOTS");
        checkBeforeWithdrawAndEquip("GRACEFUL GLOVES");
        checkBeforeWithdrawAndEquip("GRACEFUL TOP");
        checkBeforeWithdrawAndEquip("GRACEFUL LEGS");
    }

    private void withdrawDigsitePendant() {
        if (Rs2Equipment.isWearing(ItemID.DIGSITE_PENDANT_1)
                || Rs2Equipment.isWearing(ItemID.DIGSITE_PENDANT_2)
                || Rs2Equipment.isWearing(ItemID.DIGSITE_PENDANT_3)
                || Rs2Equipment.isWearing(ItemID.DIGSITE_PENDANT_4)
                || Rs2Equipment.isWearing(ItemID.DIGSITE_PENDANT_5)
        ) return;

        if (Rs2Bank.hasItem(ItemID.DIGSITE_PENDANT_1)) {
            Rs2Bank.withdrawAndEquip(ItemID.DIGSITE_PENDANT_1);
        } else if (Rs2Bank.hasItem(ItemID.DIGSITE_PENDANT_2)) {
            Rs2Bank.withdrawAndEquip(ItemID.DIGSITE_PENDANT_2);
        } else if (Rs2Bank.hasItem(ItemID.DIGSITE_PENDANT_3)) {
            Rs2Bank.withdrawAndEquip(ItemID.DIGSITE_PENDANT_3);
        } else if (Rs2Bank.hasItem(ItemID.DIGSITE_PENDANT_4)) {
            Rs2Bank.withdrawAndEquip(ItemID.DIGSITE_PENDANT_4);
        } else {
            Rs2Bank.withdrawAndEquip(ItemID.DIGSITE_PENDANT_5);
        }
    }

    private void seedHouse(WorldPoint worldPoint, states status) {
        if (Rs2Inventory.use(selectedSeed) && Rs2GameObject.interact(worldPoint)) {
            sleep(1500, 2000);
            botStatus = status;
        }
    }

    private void dismantleAndRebuildBirdhouse(int itemId, states status) {
        if (!Rs2Player.isMoving() &&
                !Rs2Player.isAnimating() &&
                !Rs2Player.isInteracting()
        ) {
            Rs2GameObject.interact(itemId, "reset");
            sleepUntil(() -> (!Rs2Player.isAnimating() && !Rs2Player.isInteracting() && Rs2Player.waitForXpDrop(Skill.CRAFTING,8000)), 10000);
            sleep(Rs2Random.between(2000, 3000));
            botStatus = status;
        }
    }
}
