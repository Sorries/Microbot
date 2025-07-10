package net.runelite.client.plugins.microbot.GeoffPlugins.lunarplankmake;

import java.util.concurrent.TimeUnit;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import net.runelite.client.util.QuantityFormatter;

public class LunarPlankMakeScript extends Script {

    public static String version = "1.0.2";
    public static String combinedMessage = "";
    public static long plankMade = 0;
    private int profitPerPlank = 0;
    private long startTime;
    private boolean useSetDelay;
    private int setDelay;
    private boolean useRandomDelay;
    private int maxRandomDelay;

    // State management
    private enum State {
        PLANKING,
        BANKING
    }

    private State currentState = State.BANKING;

    public boolean run(LunarPlankMakeConfig config) {
        startTime = System.currentTimeMillis();
        int unprocessedItemPrice = Microbot.getItemManager().search(config.ITEM().getName()).get(0).getPrice();
        int processedItemPrice = Microbot.getItemManager().search(config.ITEM().getFinished()).get(0).getPrice();
        profitPerPlank = processedItemPrice - unprocessedItemPrice;

        useSetDelay = config.useSetDelay();
        setDelay = config.setDelay();
        useRandomDelay = config.useRandomDelay();
        maxRandomDelay = config.maxRandomDelay();

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run() || !Microbot.isLoggedIn()) return;
                switch (currentState) {
                    case BANKING:
                        bank(config);
                        break;
                    case PLANKING:
                        plankItems(config);
                        break;
                }
            } catch (Exception ex) {
                Microbot.log("Exception in LunarPlankMakeScript: " + ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private void bank(LunarPlankMakeConfig config) {
        //Microbot.log("B");
        // Check if inventory already have log or else open bank
        if(!Rs2Inventory.hasItem(config.ITEM().getName(),true)) {
            if (!Rs2Bank.openBank()) return;
        }

        // Deposit Plank
        if (Rs2Inventory.hasItem(config.ITEM().getFinished(), true)) {
            Rs2Bank.depositAll(config.ITEM().getFinished());
            Rs2Inventory.waitForInventoryChanges(1800);
            sleep(800,1250);
        }

        int emptyslot = Rs2Inventory.getEmptySlots();

        // Check for coin
        if(!Rs2Inventory.hasItemAmount(ItemID.COINS_995, (emptyslot - 1) * config.ITEM().getCost())) {
            if(Rs2Bank.hasBankItem(ItemID.COINS_995,2)) {
                Rs2Bank.withdrawAllButOne(ItemID.COINS_995);
                Rs2Inventory.waitForInventoryChanges(1800);
                sleep(800,1300);
                emptyslot -= 1;
            }else{
                Microbot.log("Out of coins");
                shutdown();
                return;
            }
        }

        // Check for rune

        // Check for earth staff

        //Microbot.log(String.valueOf(emptyslot));

        // Withdraw logs
        if (Rs2Bank.hasItem(config.ITEM().getName()) && !Rs2Inventory.hasItem(config.ITEM().getName(),true)) {
            if(emptyslot == 0){return;}
            Rs2Bank.withdrawX(config.ITEM().getName(),emptyslot,true);
            Rs2Inventory.waitForInventoryChanges(1800);
            sleep(800,1300);
        }else if (Rs2Inventory.hasItem(config.ITEM().getName(),true)) {
        }else{
            Microbot.showMessage("No more " + config.ITEM().getName() + " to plank.");
            shutdown();
            return;
        }

        Rs2Bank.closeBank();
        sleep(800,1300);
        currentState = State.PLANKING;
    }

    private void plankItems(LunarPlankMakeConfig config) {
        //Microbot.log("P");
        // check if lunar spell book and have plank make runes
        if (!Rs2Magic.isLunar() || !Rs2Magic.hasRequiredRunes(Rs2Spells.PLANK_MAKE)){
            Microbot.showMessage("Not on lunar spell book or able to cast plank make");
            shutdown();
            return;
        }
        // check if inventory have required logs
        if (Rs2Inventory.hasItem(config.ITEM().getName())) {
            int initialLogCount = Rs2Inventory.count(config.ITEM().getName());
            Rs2Magic.cast(MagicAction.PLANK_MAKE);
            addDelay();
            if (Rs2Inventory.slotContains(18,config.ITEM().getName())){
                Rs2Inventory.slotInteract(18,"cast");
            }else {
                Rs2Inventory.interact(config.ITEM().getName());
            }
            sleep(3000,15000);
            Rs2Tab.switchToInventoryTab();
            sleepUntil(() -> !Rs2Inventory.hasItem(config.ITEM().getName(),true) && !Microbot.isGainingExp && !Rs2Player.isAnimating(), 120000);
            addDelay();
            int processedPlankCount = Rs2Inventory.count(config.ITEM().getFinished());
            if (processedPlankCount == initialLogCount) {
                plankMade += processedPlankCount;
                Microbot.log("Pm: " + plankMade);
                calculateProfitAndDisplay(config);
            }
        }
        currentState = State.BANKING;
    }

    private void calculateProfitAndDisplay(LunarPlankMakeConfig config) {
        double elapsedHours = (System.currentTimeMillis() - startTime) / 3600000.0;
        int plankPerHour = (int) (plankMade / elapsedHours);
        int totalProfit = profitPerPlank * (int) plankMade;
        int profitPerHour = profitPerPlank * plankPerHour;

        combinedMessage = config.ITEM().getFinished() + ": " +
                QuantityFormatter.quantityToRSDecimalStack((int) plankMade) + " (" +
                QuantityFormatter.quantityToRSDecimalStack(plankPerHour) + "/hr) | " +
                "Profit: " + QuantityFormatter.quantityToRSDecimalStack(totalProfit) + " (" +
                QuantityFormatter.quantityToRSDecimalStack(profitPerHour) + "/hr)";
    }

    private void addDelay() {
        if (useSetDelay) {
            sleep(Rs2Random.skewedRandAuto(setDelay));
        } else if (useRandomDelay) {
            sleep(Rs2Random.between(750, maxRandomDelay));
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        plankMade = 0; // Reset the count of planks made
        combinedMessage = ""; // Reset the combined message
        currentState = State.BANKING; // Reset the current state
    }
}
