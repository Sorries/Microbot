package net.runelite.client.plugins.microbot.plugindisabler;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

import java.util.concurrent.TimeUnit;

@Slf4j
public class PluginDisablerScript extends Script {
    public static String version = "1.0.0";
    public static Double minutesSinceXpGained = 0.0;
    public static int sameObjectClickCount = 0;

    private final PluginDisablerConfig config;

    private long lastXpTime = System.currentTimeMillis();
    private int lastObjectId = -1;

    @Getter
    @Setter
    private static int LastClickedObjectId = -1;

    public PluginDisablerScript(PluginDisablerConfig config) {
        this.config = config;
    }


    public boolean run() {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();


                if (config.noExp() && config.minutes() > 0) {
                    checkExpGained();
                    long now = System.currentTimeMillis();
                    double minutesSinceXpGained = (now - lastXpTime) / (60 * 1000.0);
                    System.out.printf("No exp for %.2f seconds%n", (now - lastXpTime) / 1000.0);
                    if ((now - lastXpTime) > ((long) config.minutes() * 60 * 1000)) {
                        Microbot.log("Disabling plugin due to no experience gained for " + Math.round(minutesSinceXpGained) + " minutes.");
                        disablePlugins();
                    }
                }


                if (config.noClick() && config.clicks() > 0) {
                    if (LastClickedObjectId != -1) {
                        int currentId = LastClickedObjectId;
                        if (currentId == lastObjectId) {
                            sameObjectClickCount++;
                        } else {
                            lastObjectId = currentId;
                            sameObjectClickCount = 1;
                        }
                        LastClickedObjectId = -1;
                        System.out.println("Item interacted " + LastClickedObjectId +", Last Object ID " + lastObjectId + ", Same Object Count " + sameObjectClickCount);
                        if (sameObjectClickCount > config.clicks()) {
                            Microbot.log("Disabling plugin due to repeated clicks on object ID: " + currentId);
                            disablePlugins();
                        }
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
        LastClickedObjectId = -1;
        super.shutdown();
    }

    private void disablePlugins() {
        for (Plugin plugin : Microbot.getActiveMicrobotPlugins()) {
            Microbot.stopPlugin(plugin);
        }
        Microbot.pauseAllScripts = true;
    }

    private void checkExpGained() {
        if (Microbot.isGainingExp) {
            lastXpTime = System.currentTimeMillis();
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

