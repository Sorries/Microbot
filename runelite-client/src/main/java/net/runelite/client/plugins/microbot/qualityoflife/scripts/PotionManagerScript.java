package net.runelite.client.plugins.microbot.qualityoflife.scripts;

import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.qualityoflife.QoLConfig;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.concurrent.TimeUnit;

public class PotionManagerScript extends Script {
    private boolean runOnce = false;
    private int randomPoints = 0;
    public boolean run(QoLConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (!config.enablePotionManager()) return;
                if (!Rs2Player.isInCombat()) return;

                sleep(500,1500);

                // Always attempt to drink anti-poison
                if (Rs2Player.drinkAntiPoisonPotion()) {
                    Rs2Player.waitForAnimation();
                }

                // Always attempt to drink antifire potion
                if (Rs2Player.drinkAntiFirePotion()) {
                    Rs2Player.waitForAnimation();
                }

                // Always attempt to drink prayer potion
                if (config.autoDrinkPrayerPot()){
                    handleAutoDrinkPrayPot(config.drinkPrayerPotPoints());
                }


                // Always attempt to drink ranging potion
                if (Rs2Player.drinkCombatPotionAt(Skill.RANGED, false)) {
                    Rs2Player.waitForAnimation();
                }

                // Always attempt to drink magic potion
                if (Rs2Player.drinkCombatPotionAt(Skill.MAGIC, false)) {
                    Rs2Player.waitForAnimation();
                }

                // Always attempt to drink combat potions for STR, ATT, DEF
                if (Rs2Player.drinkCombatPotionAt(Skill.STRENGTH)) {
                    Rs2Player.waitForAnimation();
                }
                if (Rs2Player.drinkCombatPotionAt(Skill.ATTACK)) {
                    Rs2Player.waitForAnimation();
                }
                if (Rs2Player.drinkCombatPotionAt(Skill.DEFENCE)) {
                    Rs2Player.waitForAnimation();
                }

                // Always attempt to drink goading potion
//                if (Rs2Player.drinkGoadingPotion()) {
//                    Rs2Player.waitForAnimation();
//                }

                if(Rs2Inventory.hasItem(ItemID.VIAL_EMPTY)) {
                    Rs2Inventory.dropAll(ItemID.VIAL_EMPTY);
                    Rs2Inventory.waitForInventoryChanges(1000);
                }

            } catch (Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    private void handleAutoDrinkPrayPot(int points) {
        if(!runOnce) {
            randomPoints = points + Rs2Random.between(-2,3);
            if (randomPoints <= 0) {return;}
            runOnce = true;
//            Microbot.log("Generated prayer drink threshold: " + randomPoints);
        }
        if (Rs2Player.getBoostedSkillLevel(Skill.PRAYER) <= randomPoints) {
            if(Rs2Player.drinkPrayerPotionAt(randomPoints)) {
//                Microbot.log("Drank at " + randomPoints + " prayer points");
                runOnce = false;
                Rs2Player.waitForAnimation();
                sleep(400);
            }
            if (Rs2Inventory.contains(229)){
                Rs2Inventory.dropAll(229);
            }
        }

    }

    // shutdown
    @Override
    public void shutdown() {
        super.shutdown();
    }

}
