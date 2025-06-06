package net.runelite.client.plugins.microbot.plugindisabler;

import net.runelite.client.plugins.microbot.BlockingEvent;
import net.runelite.client.plugins.microbot.BlockingEventPriority;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerPlugin;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.*;
import net.runelite.client.plugins.microbot.Microbot;

public class PluginDisabler implements BlockingEvent {
    public static String version = "1.0.0";
    public static Double minutesSinceXpGained;
    private final PluginDisablerConfig config;

    private long lastXpTime = System.currentTimeMillis();
    //private double minutesSinceXpGained // Track last XP gain
    private int sameObjectClickCount = 0;                 // Counter for clicks
    private int lastObjectId = -1;
    private boolean alreadyDisabled;

    public PluginDisabler(PluginDisablerConfig config) {
        this.config = config;
    }

    @Override
    public boolean validate() {
        if (config.noExp() && config.minutes()>0) {
            long now = System.currentTimeMillis();
            checkExpGained();
            if ((now - lastXpTime) > (config.minutes() * 60 * 1000)) {
                return true;
            } else {
                minutesSinceXpGained = (now - lastXpTime) / (60 *1000.0);
                //System.out.printf("No exp for %.2f seconds%n", (now - lastXpTime) / 1000.0);
            }
        }

//        Rs2GameObject currentObject = getLastClickedObject(); // Implement this in your util or store on click
//        if (currentObject != null) {
//            int currentId = currentObject.getId();
//            if (currentId == lastObjectId) {
//                sameObjectClickCount++;
//            } else {
//                lastObjectId = currentId;
//                sameObjectClickCount = 1;
//            }
//
//            return sameObjectClickCount > 50;
//        }

        return false;
    }


    @Override
    public boolean execute() {
        Microbot.log("Disabling plugin due to idle or repeated clicks.");
        Microbot.pauseAllScripts = true;
        Microbot.getBlockingEventManager().remove(this); // when turning off the script, it does not execute again
        if (Microbot.isPluginEnabled(BreakHandlerPlugin.class)){
            Microbot.stopPlugin(Microbot.getPlugin(BreakHandlerPlugin.class.getName()));
        }
        return true;
    }

    @Override
    public BlockingEventPriority priority() {
        return BlockingEventPriority.NORMAL;
    }

    public void checkExpGained() {
        if(Microbot.isGainingExp){
        lastXpTime = System.currentTimeMillis();}
    }


    private Rs2GameObject getLastClickedObject() {
        return null;
    }


}
