package net.runelite.client.plugins.microbot.sailing.features.salvaging;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.gameval.ObjectID;

import java.util.Map;
import java.util.Set;

public final class SalvageObjectIds {

    private SalvageObjectIds() {
    }

    public static final Map<Integer, Integer> SALVAGE_LEVEL_REQ = ImmutableMap.<Integer, Integer>builder()
            .put(ObjectID.SAILING_SMALL_SHIPWRECK, 15)
            .put(ObjectID.SAILING_FISHERMAN_SHIPWRECK, 26)
            .put(ObjectID.SAILING_BARRACUDA_SHIPWRECK, 35)
            .put(ObjectID.SAILING_LARGE_SHIPWRECK, 53)
            .put(ObjectID.SAILING_PIRATE_SHIPWRECK, 64)
            .put(ObjectID.SAILING_MERCENARY_SHIPWRECK, 73)
            .put(ObjectID.SAILING_FREMENNIK_SHIPWRECK, 80)
            .put(ObjectID.SAILING_MERCHANT_SHIPWRECK, 87)
            .build();

    public static final Set<Integer> ACTIVE_SHIPWRECK_IDS = SALVAGE_LEVEL_REQ.keySet();
}
