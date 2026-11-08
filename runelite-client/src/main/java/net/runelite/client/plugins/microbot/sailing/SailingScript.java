package net.runelite.client.plugins.microbot.sailing;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.sailing.features.salvaging.SalvagingScript;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class SailingScript extends Script {

    private final SalvagingScript salvagingScript;

    @Inject
    public SailingScript(SalvagingScript salvagingScript) {
        this.salvagingScript = salvagingScript;
    }

    public boolean run(SailingConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run() || !config.salvaging()) {
                    return;
                }
                salvagingScript.run(config);
            } catch (Exception ex) {
                shutdown();
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }
}
