package net.runelite.client.plugins.microbot.nateplugins.skilling.arrowmaker;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ArrowScript extends Script {

    public static double version = 1.1;

    private int sleepMin;
    private int sleepMax;
    private int sleepTarget;

    public boolean run(ArrowConfig config) {

        sleepMin = 60;
        sleepMax = 1800;
        sleepTarget = 900;

        Rs2Antiban.antibanSetupTemplates.applyGeneralBasicSetup();
        Rs2AntibanSettings.antibanEnabled = true;
        Rs2AntibanSettings.contextualVariability = true;
        Rs2AntibanSettings.usePlayStyle = true;


        Rs2AntibanSettings.simulateFatigue = true;
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.behavioralVariability = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.dynamicActivity = true;
        Rs2AntibanSettings.profileSwitching = true;

        Rs2AntibanSettings.simulateMistakes = true;
        Rs2AntibanSettings.naturalMouse = true;

        //Rs2AntibanSettings.universalAntiban = true;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            if (!Microbot.isLoggedIn()) return;
            try {
                long startTime = System.currentTimeMillis();
                if (Microbot.pauseAllScripts.get()) return;
                if (config.ARROWBool()) {
                    checkAndUseItem(config.ARROW().getItem1(), config.ARROW().getItem2());
                }
                if (config.BOLTBool()) {
                    checkAndUseItem(config.BOLT().getItem1(), config.BOLT().getItem2());
                }
                if (config.DARTBool()) {
                    checkAndUseItem(config.DART().getItem1(), config.DART().getItem2());
                }
                if (config.TIPPINGBool()) {
                    checkAndUseItem(config.TIP().getItem1(), config.TIP().getItem2());
                }
                if (config.DRAGONTIPPINGBool()) {
                    checkAndUseItem(config.DragonTIP().getItem1(), config.DragonTIP().getItem2());
                }
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    private void checkAndUseItem(String item1, String item2) {
        if (Rs2Inventory.count(item1) > 0 && Rs2Inventory.count(item2) > 0) {
            Rs2Inventory.combine(item1, item2);
            handleSleep();
        }else{
            Microbot.log("Out of Inventory Item");
            super.shutdown();
        }
    }
    private void handleSleep(){
        sleepUntilOnClientThread(() -> Rs2Widget.getWidget(17694734) != null);
        sleep(calculateSleepDuration(1));
        Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
        waitForAnimationToFullyStop();
        sleep(calculateSleepDuration(1));

    }
    @Override
    public void shutdown() {
        super.shutdown();
    }

    public void waitForAnimationToFullyStop() {
        long lastAnimatingTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - lastAnimatingTime < 3000) {
            if (Rs2Player.isAnimating()) {
                lastAnimatingTime = System.currentTimeMillis();
                //System.out.println("Animation detected, resetting timer...");
            }
            sleep(100);
        }
        //System.out.println("Animation has fully stopped!");
    }

    private int calculateSleepDuration(double multiplier) { //credit to BankStanderScript
        // Create a Random object
        Random random = new Random();

        // Calculate the mean (average) of sleepMin and sleepMax, adjusted by sleepTarget
        double mean = (sleepMin + sleepMax + sleepTarget) / 3.0;

        // Calculate the standard deviation with added noise
        double noiseFactor = 0.2; // Adjust the noise factor as needed (0.0 to 1.0)
        double stdDeviation = Math.abs(sleepTarget - mean) / 3.0 * (1 + noiseFactor * (random.nextDouble() - 0.5) * 2);

        // Generate a random number following a normal distribution
        int sleepDuration;
        do {
            // Generate a random number using nextGaussian method, scaled by standard deviation
            sleepDuration = (int) Math.round(mean + random.nextGaussian() * stdDeviation);
        } while (sleepDuration < sleepMin || sleepDuration > sleepMax); // Ensure the duration is within the specified range
        if ((int) Math.round(sleepDuration * multiplier) < 60)
            sleepDuration += ((60 - sleepDuration) + Rs2Random.between(11, 44));
        return sleepDuration;
    }
}
