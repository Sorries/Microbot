package net.runelite.client.plugins.microbot.GeoffPlugins.lunarplankmake.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Logs {

    LOGS("Logs", "Plank",70),
    OAK_LOGS("Oak logs", "Oak plank",175),
    TEAK_LOGS("Teak logs", "Teak plank",350),
    MAHOGANY_LOGS("Mahogany logs", "Mahogany plank",1050);

    private final String name;
    @Getter
    private final String finished;
    private final int cost;


    @Override
    public String toString() {
        return name;
    }
}
