package net.runelite.client.plugins.microbot.nmz;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.aiofighter.combat.PotionManagerScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.security.Encryption;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.security.LoginManager;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static net.runelite.api.Varbits.NMZ_ABSORPTION;

public class NmzScript extends Script {

    public static double version = 2.0;

    public static NmzConfig config;

    public static boolean useOverload = false;
    private boolean firstIterationOfOverLoad = true;


    public static int maxHealth = Rs2Random.between(2, 8);
    public static int minAbsorption = Rs2Random.between(100, 300);

    private boolean hasWalkedToCenter = false;

    private WorldPoint center = new WorldPoint(Rs2Random.between(2270, 2276), Rs2Random.between(4693, 4696), 0);

    @Getter
    @Setter
    private static boolean hasSurge = false;

    public boolean canStartNmz() {
        return Rs2Inventory.count("overload (4)") == config.overloadPotionAmount() && Rs2Inventory.count("absorption (4)") == config.absorptionPotionAmount() ||
                (Rs2Inventory.hasItem("prayer potion") && config.togglePrayerPotions());
    }


    public boolean run(NmzConfig config) {
        NmzScript.config = config;
        Microbot.getSpecialAttackConfigs().setSpecialAttack(true);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                Rs2Combat.enableAutoRetialiate();
                if (Rs2Random.between(1, 50) == 1 && config.randomMouseMovements()) {
                    Microbot.getMouse().click(Rs2Random.between(0, Microbot.getClient().getCanvasWidth()), Rs2Random.between(0, Microbot.getClient().getCanvasHeight()), true);
                }
                boolean isOutsideNmz = Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(2602, 3116, 0)) < 20;
                useOverload = Microbot.getClient().getBoostedSkillLevel(Skill.RANGED) == Microbot.getClient().getRealSkillLevel(Skill.RANGED) && config.overloadPotionAmount() > 0;
                if (isOutsideNmz) {
                    Rs2Walker.setTarget(null);
                    hasWalkedToCenter = false;
                    firstIterationOfOverLoad = true;
                    handleOutsideNmz();
                } else {
                    handleInsideNmz();
                }
            } catch (Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    public void handleOutsideNmz() {
        boolean hasStartedDream = Microbot.getVarbitValue(3946) > 0;
        if (config.togglePrayerPotions())
            Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, false);
        if (!hasStartedDream) {
            startNmzDream();
        } else {
            final String overload = "Overload (4)";
            final String absorption = "Absorption (4)";
            storePotions(ObjectID.OVERLOAD_POTION, "overload", config.overloadPotionAmount());
            storePotions(ObjectID.ABSORPTION_POTION, "absorption", config.absorptionPotionAmount());
            handleStore();
            fetchPotions(ObjectID.OVERLOAD_POTION, overload, config.overloadPotionAmount());
            fetchPotions(ObjectID.ABSORPTION_POTION, absorption, config.absorptionPotionAmount());
            if (canStartNmz()) {
                consumeEmptyVial();
                sleep(Rs2Random.between(1500, 3000));
            } else {
                sleep(2000);
            }
        }
    }

    public void handleInsideNmz() {
        if (config.togglePrayerPotions())
            Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
        if (!useOrbs() && config.walkToCenter()) {
            walkToCenter();
        }
        if (!hasWalkedToCenter) {
            walkToCenter();
            hasWalkedToCenter = true;
        }
        useOverloadPotion();
        manageSelfHarm();
        useAbsorptionPotion();
        attackNearbyMonster();
    }

    private void attackNearbyMonster(){
        if(!Rs2Combat.inCombat()) {
            int waited = 0;
            int timeout = Rs2Random.between(10000, 30000);
            //System.out.println(timeout);
            while (waited < timeout) {
                if (Rs2Combat.inCombat()) {
                    break;
                }
                sleep(250);
                waited += 250;
            }
            if (!Rs2Combat.inCombat()) {
                Optional<Rs2NpcModel> monster = Rs2Npc.getAttackableNpcs()
                        .filter(o -> true)
                        .findFirst();
                if (monster.isPresent()) {
                    Rs2Npc.attackInInstance(monster.get());
                    Microbot.log("Attacked "+monster.get().getName());
                }
            }
        }
    }

    private void walkToCenter() {
        if (center.distanceTo(Rs2Player.getWorldLocation()) > 4) {
            Rs2Walker.walkTo(center, 6);
            sleepUntil(() -> !Rs2Player.isMoving(), Rs2Random.between(3000, 5000));
        }
    }

    public void startNmzDream() {
        // Set new center so that it is random for every time joining the dream
        center = new WorldPoint(Rs2Random.between(2270, 2276), Rs2Random.between(4693, 4696), 0);
        Rs2Npc.interact(NpcID.DOMINIC_ONION, "Dream");
        sleepUntil(() -> Rs2Widget.hasWidget("Which dream would you like to experience?"));
        Rs2Widget.clickWidget("Previous:");
        sleepUntil(() -> Rs2Widget.hasWidget("Click here to continue"));
        Rs2Widget.clickWidget("Click here to continue");
        sleepUntil(() -> Rs2Widget.hasWidget("Agree to pay"));
        if (Rs2Widget.hasWidget("Agree to pay")) {
            Rs2Keyboard.typeString("1");
            Rs2Keyboard.enter();
        }
    }

    public boolean useOrbs() {
        boolean orbHasSpawned = false;
        if (config.useZapper()) {

            orbHasSpawned = interactWithObject(ObjectID.ZAPPER_26256);
        }
        if (config.useReccurentDamage()) {

            orbHasSpawned = interactWithObject(ObjectID.RECURRENT_DAMAGE);
        }

        if (config.usePowerSurge()) {

            orbHasSpawned = interactWithObject(ObjectID.POWER_SURGE);
        }

        return orbHasSpawned;
    }

    public boolean interactWithObject(int objectId) {
        TileObject rs2GameObject = Rs2GameObject.findObjectById(objectId);
        if (rs2GameObject != null) {
            sleep(Rs2Random.between(2000, 10000));
            Microbot.log("Distance between: " + Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(rs2GameObject.getWorldLocation()));
            if(Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(rs2GameObject.getWorldLocation()) > 10) {
                Rs2Walker.walkFastLocal(rs2GameObject.getLocalLocation());
                sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(rs2GameObject.getWorldLocation()) < 10);
            }
            Rs2GameObject.interact(objectId);
            return true;
        }
        return false;
    }

    public void manageSelfHarm() {
        //Microbot.log("ManageSelfHarm");
        int currentHP = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS);
        int currentRangedLevel = Microbot.getClient().getBoostedSkillLevel(Skill.RANGED);
        int realRangedLevel = Microbot.getClient().getRealSkillLevel(Skill.RANGED);
        int overloadTimeLeft = Microbot.getClientThread().runOnClientThreadOptional(() ->Microbot.getClient().getVarbitValue(VarbitID.NZONE_OVERLOAD_POTION_EFFECTS)).orElse(0);
        boolean hasOverloadPotions = config.overloadPotionAmount() > 0;
//useOverload = Microbot.getClient().getBoostedSkillLevel(Skill.RANGED) == Microbot.getClient().getRealSkillLevel(Skill.RANGED) && config.overloadPotionAmount() > 0;
        // true = not active , false = active overload

        if (currentHP >= maxHealth
                && (!useOverload || overloadTimeLeft > 0) // false = not active overload , true = active overload
                && (!hasOverloadPotions || currentRangedLevel != realRangedLevel)
                ) {
            Microbot.log("!useOverload: " + !useOverload + " overloadTimeLeft: " + overloadTimeLeft);
            maxHealth = 1;
            Microbot.log("Current Hp: " + currentHP);
            if (Rs2Inventory.hasItem(ItemID.LOCATOR_ORB)) {
                Rs2Inventory.interact(ItemID.LOCATOR_ORB, "feel");
                sleep(100,300);
            } else if (Rs2Inventory.hasItem(ItemID.DWARVEN_ROCK_CAKE_7510)) {
                Rs2Inventory.interact(ItemID.DWARVEN_ROCK_CAKE_7510, "guzzle");
                sleep(100,300);
            }

            if (currentHP == 1) {
                maxHealth = Rs2Random.between(2, 4);
            }
        }

        if (config.randomlyTriggerRapidHeal()) {
            randomlyToggleRapidHeal();
        }
    }

    public void randomlyToggleRapidHeal() {
        if (Rs2Random.between(1, 50) == 2) {
            Rs2Prayer.toggle(Rs2PrayerEnum.RAPID_HEAL, true);
            sleep(500, 800);
            Rs2Prayer.toggle(Rs2PrayerEnum.RAPID_HEAL, false);
        }
    }

    public void useOverloadPotion() {
        if (useOverload && Rs2Inventory.hasItem("overload") && Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) > 50) {
            if (!firstIterationOfOverLoad) {
                sleep(Rs2Random.between(3000, 10000));
            }
            Rs2Inventory.interact(x -> x.getName().toLowerCase().contains("overload"), "drink");
            sleep(10000);
            firstIterationOfOverLoad = false; // Mark first iteration as completed
        }
        if (!Rs2Inventory.hasItem("overload","prayer")){
            Microbot.log("No overload or prayer potion found");
            shutdown();
        }
    }

    public void useAbsorptionPotion() {
        if (Microbot.getVarbitValue(NMZ_ABSORPTION) < minAbsorption && Rs2Inventory.hasItem("absorption")) {
            for (int i = 0; i < Rs2Random.between(4, 8); i++) {
                Rs2Inventory.interact(x -> x.getName().toLowerCase().contains("absorption"), "drink");
                sleep(1000, 1500);
            }
//            while (Microbot.getVarbitValue(NMZ_ABSORPTION) <= 1000 && isRunning()){
//                Rs2Inventory.interact(x -> x.getName().toLowerCase().contains("absorption"), "drink");
//                sleep(1000, 1500);
//            }
            minAbsorption = Rs2Random.between(100, 500);
        }
    }

    private void storePotions(int objectId, String itemName, int requiredAmount) {
        if (Rs2Inventory.count(itemName) == requiredAmount) return;
        if (Rs2Inventory.get(itemName) == null) return;

        Rs2GameObject.interact(objectId, "Store");
        String storeWidgetText = "Store all your ";
        sleepUntil(() -> Rs2Widget.hasWidget(storeWidgetText));
        if (Rs2Widget.hasWidget(storeWidgetText)) {
            Rs2Keyboard.typeString("1");
            Rs2Keyboard.enter();
            sleepUntil(() -> !Rs2Inventory.hasItem(objectId));
            Rs2Inventory.dropAll(itemName);
        }
    }

    private void fetchPotions(int objectId, String itemName, int requiredAmount) {
        if (Rs2Inventory.count(itemName) == requiredAmount) return;

        Rs2GameObject.interact(objectId, "Take");
        String widgetText = "How many doses of ";
        sleepUntil(() -> Rs2Widget.hasWidget(widgetText));
        if (Rs2Widget.hasWidget(widgetText)) {
            sleep(800, 1300);
            Rs2Keyboard.typeString(Integer.toString(requiredAmount * 4));
            sleep(500, 1000);
            Rs2Keyboard.enter();
            sleepUntil(() -> Rs2Inventory.count(itemName) == requiredAmount);
        }
    }

    public void consumeEmptyVial() {
        final int EMPTY_VIAL = 26291;
        if (Microbot.getClientThread().runOnClientThreadOptional(() -> Rs2Widget.getWidget(129, 6) == null || Rs2Widget.getWidget(129, 6).isHidden()).orElse(false)) {
            Rs2GameObject.interact(EMPTY_VIAL, "drink");
        }
        Widget widget = Rs2Widget.getWidget(129, 6);
        if (!Microbot.getClientThread().runOnClientThreadOptional(widget::isHidden).orElse(false)) {
            Rs2Widget.clickWidget(widget.getId());
            sleep(300);
            Rs2Widget.clickWidget(widget.getId());
        }
    }

    public void handleStore() {
        if (canStartNmz()) return;
        int varbitOverload = 3953;
        int varbitAbsorption = 3954;
        int overloadAmt = Microbot.getVarbitValue(varbitOverload);
        int absorptionAmt = Microbot.getVarbitValue(varbitAbsorption);
        int nmzPoints = Microbot.getVarbitPlayerValue(VarPlayer.NMZ_REWARD_POINTS);

        if (absorptionAmt > config.absorptionPotionAmount() * 4 && overloadAmt > config.overloadPotionAmount() * 4)
            return;

        if (!Rs2Inventory.isFull()) {
            if ((absorptionAmt < (config.absorptionPotionAmount() * 4) || overloadAmt < config.overloadPotionAmount() * 4) && nmzPoints < 100000) {
                Microbot.showMessage("BOT SHUTDOWN: Not enough points to buy potions");
                shutdown();
                return;
            }
        }

        Microbot.showMessage("Please purchase overload / absorption potion");
        shutdown();
//        Rs2GameObject.interact(26273);
//        sleepUntil(() -> Rs2Widget.isWidgetVisible(13500418) || Rs2Bank.isBankPinWidgetVisible(), 10000);
//        if (Rs2Bank.isBankPinWidgetVisible()) {
//            try {
//                Rs2Bank.handleBankPin(Encryption.decrypt(LoginManager.getActiveProfile().getBankPin()));
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            sleepUntil(() -> Rs2Widget.isWidgetVisible(13500418), 10000);
//        }
//
//        Widget benefitsBtn = Rs2Widget.getWidget(13500418);
//        if (benefitsBtn == null) return;
//        boolean notSelected = benefitsBtn.getSpriteId() != 813;
//        if (notSelected) {
//            Rs2Widget.clickWidgetFast(benefitsBtn, 4, 4);
//        }
//        int count = 0;
//        while (count < Rs2Random.between(3, 5)) {
//            Widget nmzRewardShop = Rs2Widget.getWidget(206, 6);
//            if (nmzRewardShop == null) break;
//            Widget overload = nmzRewardShop.getChild(6);
//            Rs2Widget.clickWidgetFast(overload, 6, 4, overload.getName(),"Buy-50");
//            sleep(600, 1200);
//            Widget absorption = nmzRewardShop.getChild(9);
//            Rs2Widget.clickWidgetFast(absorption, 9, 4, absorption.getName(),"Buy-50");
//            sleep(600, 1200);
//            count++;
//        }
    }

}
