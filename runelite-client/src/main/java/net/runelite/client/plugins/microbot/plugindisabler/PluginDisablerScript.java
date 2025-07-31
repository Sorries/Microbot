package net.runelite.client.plugins.microbot.plugindisabler;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.Notifier;
import net.runelite.client.config.Notification;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.poh.PohTeleports;


import javax.inject.Inject;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PluginDisablerScript extends Script {
    public static String version = "1.0.0";
    public static Double minutesSinceXpGained = 0.0;
    public static int sameObjectClickCount = 0;
    public static int cantReachCounter = 0;
    public static long lastXpTime = System.currentTimeMillis();
    public static long currentTime = System.currentTimeMillis();
    public static double minutesLeft;
    public static boolean disablePluginsFlag = true;
    public static long timeThresholdMinutes;
    public final List<Instant> cantReachTimestamps = new ArrayList<>();

    private SimpleBreakHandler breakHandler;
    private final PluginDisablerConfig config;

    private int lastObjectId = -1;

    @Setter
    private long lastTimeConfigValue = -1;

    @Getter
    @Setter
    private static int lastClickedObjectId = -1;

    @Getter
    @Setter
    private long startTime2;
    @Setter
    @Getter
    public static boolean lockState = false;
    @Getter
    private static PluginDisablerScript instance;

    @Inject
    public PluginDisablerScript(PluginDisablerConfig config) {
        this.config = config;
    }

    @Inject
    private Notifier notifier;


    public boolean run() {
        instance = this;
        startTime2 = System.currentTimeMillis();
        if (config.useBreaks()) {
            breakHandler = new SimpleBreakHandler(
                    config.minPlaytime(),
                    config.maxPlaytime(),
                    config.minBreaktime(),
                    config.maxBreaktime()
            );
        }
        //Microbot.log("breakIn after initialization: " + breakHandler.getBreakIn());

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (breakHandler != null) {
                    breakHandler.tick();
                    if (breakHandler.isBreaking()) {
                        return;
                    }
                }
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();



                checkExpGained();
                if (config.noExp() && config.minutes() > 0) {
                    long now = System.currentTimeMillis();
                    minutesSinceXpGained = (now - lastXpTime) / (60 * 1000.0);
                    //System.out.printf("No exp for %.2f seconds%n", (now - lastXpTime) / 1000.0);
                    if ((now - lastXpTime) > ((long) config.minutes() * 60 * 1000)) {
                        Microbot.log("Disabling plugin due to no experience gained for " + Math.round(minutesSinceXpGained) + " minutes.");
                        disablePlugins();
                    }
                }

                if (config.noClick() && config.clicks() > 0) {
                    if (lastClickedObjectId != -1) {
                        int currentId = lastClickedObjectId;
                        if (currentId == lastObjectId) {
                            sameObjectClickCount++;
                        } else {
                            lastObjectId = currentId;
                            sameObjectClickCount = 1;
                        }
                        System.out.println("Item interacted " + lastClickedObjectId +", Last Object ID " + lastObjectId + ", Same Object Count " + sameObjectClickCount);
                        lastClickedObjectId = -1; // reset it once to prevent spam of interacted

                        if (sameObjectClickCount > config.clicks()) {
                            Microbot.log("Disabling plugin due to repeated clicks on object ID: " + currentId);
                            disablePlugins();
                        }
                    }
                }

//                if(config.cantReach() && config.cantReachNumber()>0){
//                    Microbot.log("Cant Reach Counter "+cantReachCounter);
//                    if (cantReachCounter >= config.cantReachNumber()) {
//                        Microbot.log("Disabling plugin due to repeated 'I cant reach that!'");
//                        cantReachCounter = 0;
//                        disablePlugins();
//                    }
//                }

                if (config.cantReach() && config.cantReachNumber() > 0) {
                    Instant now = Instant.now();
                    cantReachTimestamps.removeIf(t -> Duration.between(t, now).toMinutes() >= 10);
                    System.out.println("Cant Reach " + cantReachTimestamps);

                    Microbot.log("'Cant Reach' count in last 10 minutes: " + cantReachTimestamps.size());

                    if (cantReachTimestamps.size() >= config.cantReachNumber()) {
                        Microbot.log("Disabling plugin due to repeated 'I can't reach that!' within 15 minutes");
                        cantReachTimestamps.clear();
                        disablePlugins();
                    }
                }

                if (config.noTime() && config.time() > 0) {
                    long now = System.currentTimeMillis();
                    long currentTimeConfigValue = config.time();
                    if (currentTimeConfigValue != lastTimeConfigValue) {
                        timeThresholdMinutes = currentTimeConfigValue + Rs2Random.betweenInclusive(-5, 5);
                        lastTimeConfigValue = currentTimeConfigValue;
                    }
                    //System.out.println("now: " + now + " startTime2: " + startTime2 + " Difference: " + String.format("%.2f", (now-startTime2)/(60*1000.0)) + " Threshold: " + String.format("%.2f", (double) timeThresholdMinutes));
                    minutesLeft = Math.max(0, ((timeThresholdMinutes * 60 * 1000L) - (now - startTime2)) / (1000 * 60));
                    if ((now - startTime2) > (timeThresholdMinutes * 60 * 1000L)) {
                        Microbot.log("Disabling plugin due to time limit reached");
                        disablePlugins();
                    }
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                // System.out.println("Total time for loop: " + totalTime);

            } catch (Exception ex) {
                log.error("Error in PluginDisablerScript", ex);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS); // Adjust delay as needed
        return true;
    }

    @Override
    public void shutdown() {
        sameObjectClickCount = 0;
        lastObjectId = -1;
        lastClickedObjectId = -1;
        setBreakIn(0);
        setBreakDuration(0);
        lastTimeConfigValue = -1;
        cantReachTimestamps.clear();
        super.shutdown();
    }

    private void disablePlugins() {
        for (Plugin plugin : Microbot.getActiveMicrobotPlugins()) {
            Microbot.stopPlugin(plugin);
        }
        Microbot.pauseAllScripts.set(true);
        disablePluginsFlag = false;
        setLockState(true);
        if (config.teleOut()){
            sleepUntil(()-> !Rs2Player.isMoving() && !Rs2Player.isInteracting(),20000);
            if(Rs2Inventory.contains(8013)) {
                Rs2Inventory.interact(8013, "break");
                sleepUntil(PohTeleports::isInHouse);
            }else if (Rs2Bank.walkToBankAndUseBank()){
                if(Rs2Bank.isOpen()){
                    if(Rs2Inventory.emptySlotCount() <= 1) {
                        Rs2Bank.depositAll();
                        Rs2Inventory.waitForInventoryChanges(1000);
                    }
                    Rs2Bank.withdrawOne(8013);
                    Rs2Inventory.waitForInventoryChanges(1000);
                    Rs2Bank.closeBank();
                    sleepUntil(() -> !Rs2Bank.isOpen());
                    Rs2Inventory.interact(8013, "break");
                    sleepUntil(PohTeleports::isInHouse);
                }
            }
        }
        notifier.notify(Notification.ON, "Plugin Disabled.");
    }

    private void checkExpGained() {
        if (Microbot.isGainingExp) {
            lastXpTime = System.currentTimeMillis();
        }
    }

    public int getBreakIn() {
        return breakHandler != null ? breakHandler.getBreakIn() : -1;
    }

    public int getBreakDuration() {
        return breakHandler != null ? breakHandler.getBreakDuration() : -1;
    }
    public void setBreakIn(int value) {
        if (breakHandler != null) {
            breakHandler.setBreakIn(value);
        }
    }

    public void setBreakDuration(int value) {
        if (breakHandler != null) {
            breakHandler.setBreakDuration(value);
        }
    }
    public void scheduleNextBreak() {
        if (breakHandler != null) {
            breakHandler.scheduleNextBreak();
        }
    }



    public class SimpleBreakHandler {
        private final int minPlaytime;
        private final int maxPlaytime;
        private final int minBreaktime;
        private final int maxBreaktime;
        @Getter
        @Setter
        private int breakIn;
        @Getter
        @Setter
        private int breakDuration;

        public SimpleBreakHandler(int minPlaytime, int maxPlaytime, int minBreaktime, int maxBreaktime) {
            this.minPlaytime = minPlaytime;
            this.maxPlaytime = maxPlaytime;
            this.minBreaktime = minBreaktime;
            this.maxBreaktime = maxBreaktime;
            scheduleNextBreak();
            //System.out.println("starting");
        }

        public void tick() {
            if (breakIn > 0 && breakDuration <= 0) {
                breakIn--;
                //System.out.println("breakIn: " + breakIn);
            }
            if (!isLockState() && config.useBreaks()) {
                if (breakIn <= 0 && breakDuration <= 0) {
                    startBreak();
                }
            }
            if (breakDuration > 0) {
                breakDuration--;
                //System.out.println("breakDuration: " + breakDuration);
            }
            if (!isLockState() && config.useBreaks()) {
                if (breakDuration <= 0 && breakIn <= 0) {
                    lastXpTime = System.currentTimeMillis();
                    Microbot.pauseAllScripts.set(false);
                    System.out.println("Break Ended");
                    scheduleNextBreak();
                }
            }
        }

        private void scheduleNextBreak() {
            breakIn = Rs2Random.between(minPlaytime * 60, maxPlaytime * 60);
            Microbot.log("Next break scheduled in " + breakIn / 60 + " minutes.");
        }

        private void startBreak() {
            breakDuration = Rs2Random.between(minBreaktime * 60, maxBreaktime * 60);
            Microbot.log("Taking a break for: " + breakDuration/60 + " minutes.");
            Microbot.pauseAllScripts.set(true);
        }

        public boolean isBreaking() {
            return breakDuration > 0;
        }
    }

}


//package net.runelite.client.plugins.microbot.plugindisabler;
//
//import lombok.Getter;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import net.runelite.client.plugins.microbot.BlockingEvent;
//import net.runelite.client.plugins.microbot.BlockingEventPriority;
//import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerPlugin;
//import net.runelite.client.plugins.microbot.util.Global;
//import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
//import net.runelite.client.plugins.microbot.util.*;
//import net.runelite.client.plugins.microbot.Microbot;
//
//import net.runelite.client.plugins.*;
//@Slf4j
//public class PluginDisabler implements BlockingEvent {
//    public static String version = "1.0.0";
//    public static Double minutesSinceXpGained;
//    public static int sameObjectClickCount = 0;
//    private final PluginDisablerConfig config;
//
//    private long lastXpTime = System.currentTimeMillis();
//    private int lastObjectId = -1;
//
//    @Getter
//    @Setter
//    private static int LastClickedObjectId = -1;
//
//    public PluginDisabler(PluginDisablerConfig config) {
//        this.config = config;
//    }
//
//    @Override
//    public boolean validate() {
//        if (config.noExp() && config.minutes()>0) {
//            long now = System.currentTimeMillis();
//            checkExpGained();
//            if ((now - lastXpTime) > ((long) config.minutes() * 60 * 1000)) {
//                return true;
//            } else {
//                minutesSinceXpGained = (now - lastXpTime) / (60 *1000.0);
//                //System.out.printf("No exp for %.2f seconds%n", (now - lastXpTime) / 1000.0);
//            }
//        }
//
//        if(config.noClick() && config.clicks()>0) {
//            if (LastClickedObjectId != -1) {
//
//                int currentId = LastClickedObjectId;
//                if (currentId == lastObjectId) {
//                    sameObjectClickCount++;
//                } else {
//                    lastObjectId = currentId;
//                    sameObjectClickCount = 1;
//                }
//                System.out.println("Item interacted " + LastClickedObjectId +", Last Object ID " + lastObjectId + ", Same Object Count " + sameObjectClickCount);
//                setLastClickedObjectId(-1);
//                if (sameObjectClickCount > config.clicks()) {
//                    System.out.println("Same object clicked more than " + config.clicks() + " times.");
//                    return true;
//                }
//            }
//        }
//
//
//        return false;
//    }
//
//
//    @Override
//    public boolean execute() {
//        Microbot.log("Disabling plugin due to idle or repeated clicks.");
//        stopActiveMicrobotPlugins();
//        Microbot.pauseAllScripts = true;
//        Microbot.getBlockingEventManager().remove(this); // when turning off the script, it does not execute again
////        if (Microbot.isPluginEnabled(BreakHandlerPlugin.class)){
////            Microbot.stopPlugin(Microbot.getPlugin(BreakHandlerPlugin.class.getName()));
////        }
//        sameObjectClickCount = 0;
//        lastObjectId = -1;
//        LastClickedObjectId = -1;
//        return true;
//    }
//
//    @Override
//    public BlockingEventPriority priority() {
//        return BlockingEventPriority.NORMAL;
//    }
//
//    public void checkExpGained() {
//        if(Microbot.isGainingExp){
//        lastXpTime = System.currentTimeMillis();}
//    }
//
//    public static void stopActiveMicrobotPlugins() {
//        for (Plugin plugin : Microbot.getActiveMicrobotPlugins())
//        {
//            Microbot.stopPlugin(plugin);
//        }
//    }
//
//}

