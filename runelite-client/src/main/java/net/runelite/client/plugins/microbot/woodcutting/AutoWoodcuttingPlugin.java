package net.runelite.client.plugins.microbot.woodcutting;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.inventory.InteractOrder;
import net.runelite.client.plugins.microbot.woodcutting.enums.ForestryEvents;
import net.runelite.client.ui.overlay.OverlayManager;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "Auto Woodcutting",
        description = "Microbot woodcutting plugin",
        tags = {"Woodcutting", "microbot", "skilling"},
        enabledByDefault = false
)
@Slf4j
public class AutoWoodcuttingPlugin extends Plugin {
    @Inject
    @Getter(AccessLevel.MODULE)
    public AutoWoodcuttingScript autoWoodcuttingScript;
    @Inject
    public AutoWoodcuttingConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AutoWoodcuttingOverlay woodcuttingOverlay;


    // Forestry event variables
    public final List<Rs2NpcModel> ritualCircles = new ArrayList<>();
    public ForestryEvents currentForestryEvent = ForestryEvents.NONE;
    public final GameObject[] saplingOrder = new GameObject[3];
    public final List<GameObject> saplingIngredients = new ArrayList<>(5);
    
    // thread-safe counter for completed forestry events
    private final AtomicInteger completedForestryEvents = new AtomicInteger(0);

    private static final Pattern WOOD_CUT_PATTERN = Pattern.compile("You get (?:some|an)[\\w ]+(?:logs?|mushrooms)\\.");

    @Provides
    AutoWoodcuttingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoWoodcuttingConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(woodcuttingOverlay);
        }

    }

    protected void shutDown() {
        autoWoodcuttingScript.shutdown();
        ritualCircles.clear();
        currentForestryEvent = ForestryEvents.NONE;
        completedForestryEvents.set(0);
        overlayManager.remove(woodcuttingOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.SPAM
                && event.getType() != ChatMessageType.GAMEMESSAGE
                && event.getType() != ChatMessageType.MESBOX) {
            return;
        }

        final var msg = event.getMessage();
        if (WOOD_CUT_PATTERN.matcher(msg).matches()) {
            woodcuttingOverlay.incrementLogsChopped();
        }

        if (msg.equals("you can't light a fire here.")) {
            autoWoodcuttingScript.cannotLightFire = true;
        }

        if (msg.startsWith("The sapling seems to love")) {
            int ingredientNum = msg.contains("first") ? 1 : (msg.contains("second") ? 2 : (msg.contains("third") ? 3 : -1));
            if (ingredientNum == -1) {
                log.debug("unable to find ingredient index from message: {}", msg);
                return;
            }

            GameObject ingredientObj = this.saplingIngredients.stream()
                    .filter(obj -> {
                        String compositionName = Rs2GameObject.getCompositionName(obj).orElse(null);
                        return compositionName != null && msg.contains(compositionName.toLowerCase());
                    })
                    .findAny()
                    .orElse(null);
            if (ingredientObj == null) {
                log.debug("unable to find ingredient from message: {}", msg);
                return;
            }

            this.saplingOrder[ingredientNum - 1] = ingredientObj;
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        int id = npc.getId();
        if (id >= NpcID.GATHERING_EVENT_ENCHANTED_RITUAL_A_1 && id <= NpcID.GATHERING_EVENT_ENCHANTED_RITUAL_D_4) {
            this.ritualCircles.add(new Rs2NpcModel(npc));
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        NPC npc = event.getNpc();
        int id = npc.getId();
        if (id >= NpcID.GATHERING_EVENT_ENCHANTED_RITUAL_A_1 && id <= NpcID.GATHERING_EVENT_ENCHANTED_RITUAL_D_4) {
            this.ritualCircles.removeIf(n -> n.getIndex() == npc.getIndex());
        }
    }

    @Subscribe
    public void onGameObjectSpawned(final GameObjectSpawned event) {
        GameObject gameObject = event.getGameObject();
        switch (gameObject.getId()) {
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_1:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_2:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_3:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_4A:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_4B:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_4C:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_5:
                this.saplingIngredients.add(gameObject);
                break;
        }
    }

    @Subscribe
    public void onGameObjectDespawned(final GameObjectDespawned event) {
        final GameObject object = event.getGameObject();

        switch (object.getId()) {
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_1:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_2:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_3:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_4A:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_4B:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_4C:
            case ObjectID.GATHERING_EVENT_SAPLING_INGREDIENT_5:
                this.saplingIngredients.remove(object);
                break;
        }
    }


    
    public void incrementForestryEventCompleted() {
        completedForestryEvents.incrementAndGet();
    }
    
    public int getCompletedForestryEventCount() {
        return completedForestryEvents.get();
    }
    
    /**
     * Ensures inventory has space for forestry event rewards by dropping logs if needed
     * @param requiredSlots minimum number of free slots needed
     * @return true if enough space was made available
     */
    public boolean ensureInventorySpace(int requiredSlots) {
        int currentFreeSlots = 28 - Rs2Inventory.count();
        if (currentFreeSlots >= requiredSlots) {
            return true;
        }
        
        String logName = config.TREE().getLog();
        int slotsNeeded = requiredSlots - currentFreeSlots;
        int logsToDelete = Math.min(slotsNeeded, Rs2Inventory.count(logName));
        
        if (logsToDelete <= 0) {
            log.warn("Cannot make inventory space - no logs to drop");
            return false;
        }
        
        log.info("Making space for forestry rewards: dropping {} logs", logsToDelete);
        
        int actualDropped = Rs2Inventory.dropAmount(logName, logsToDelete, InteractOrder.EFFICIENT_ROW);
        
        sleepUntil(() -> (28 - Rs2Inventory.count()) >= requiredSlots, 2000);
        
        boolean success = (28 - Rs2Inventory.count()) >= requiredSlots;
        if (!success) {
            log.warn("Failed to create enough inventory space: dropped {} logs but still need {} slots", 
                actualDropped, requiredSlots);
        }
        
        return success;
    }
}
