package net.runelite.client.plugins.microbot.bee.chaosaltar;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.api.events.*;
import net.runelite.api.*;

import javax.inject.Inject;
import java.time.Duration;

@PluginDescriptor(
        name = PluginDescriptor.Bee + "Chaos Altar",
        description = "Automates bone offering at the Chaos Altar",
        tags = {"prayer", "bones", "altar"},
        enabledByDefault = false
)
@Slf4j
public class ChaosAltarPlugin extends Plugin {
    @Inject
    private ChaosAltarScript chaosAltarScript;
    @Inject
    private ChaosAltarConfig config;
    @Provides
    ChaosAltarConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ChaosAltarConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    ChaosAltarOverlay chaosAltarOverlay;

//    @Subscribe
//    public void onActorDeath(ActorDeath actorDeath) {
//        if (actorDeath.getActor() == Microbot.getClient().getLocalPlayer()) {
//            // set a flag, do something etc
//        }
//    }



    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) return;

        if (event.getMessage().equalsIgnoreCase("oh dear, you are dead!")) {
            Microbot.log("Dead");
            if (chaosAltarScript != null) {
                chaosAltarScript.setCurrentState(State.BANK);
            }
        }
    }


    @Override
    protected void startUp() {
        chaosAltarScript.run(config);
        if (overlayManager != null) {
            overlayManager.add(chaosAltarOverlay);
        }
    }

    @Override
    protected void shutDown() {
        chaosAltarScript.shutdown();
        overlayManager.remove(chaosAltarOverlay);
    }

    public Duration getStartTime()
    {
        return chaosAltarScript.getRunTime();
    }

}
