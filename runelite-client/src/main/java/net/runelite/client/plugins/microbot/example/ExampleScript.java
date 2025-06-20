package net.runelite.client.plugins.microbot.example;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.plugindisabler.PluginDisablerPlugin;
import net.runelite.client.plugins.microbot.storm.plugins.PlayerMonitor.PlayerMonitorPlugin;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.SmartSlayer;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

public class ExampleScript extends Script {

    public static boolean test = false;

    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                Microbot.stopPlugin(Microbot.getPlugin(PluginDisablerPlugin.class.getName()));
                System.out.println(""+ Microbot.getPlugin(PluginDisablerPlugin.class.getName()));
//                String result = String.valueOf(SmartSlayer.isCompletedSlayerTask());
//                System.out.println(result);
//                if (SmartSlayer.getCompletedSlayerTask()) {
//                    Microbot.log("Slayer task is complete!");
//                }else{
//                    Microbot.log("Slayer task is not complete!");
//                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
    }
}