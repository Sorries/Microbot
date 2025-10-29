package net.runelite.client.plugins.microbot.qualityoflife.scripts;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.qualityoflife.QoLConfig;
import net.runelite.client.plugins.microbot.qualityoflife.QoLFlashOverlay;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerChatDetectionScript {
	private static QoLFlashOverlay flashOverlay;

	public static void onChatMessage(ChatMessage event, QoLConfig config) {
		if (!config.detectPlayerChat()) {
			return;
		}

		// Only detect chat from other players
		if (event.getType() != ChatMessageType.PUBLICCHAT && 
			event.getType() != ChatMessageType.AUTOTYPER) {
			return;
		}

		// Get all players within the specified distance
		WorldPoint localLocation = Rs2Player.getWorldLocation();
		int maxDistance = config.detectPlayerChatDistance();
		
		List<Player> nearbyPlayers = getNearbyPlayers(localLocation, maxDistance);
		
		// Check if the message sender is a nearby player
		String messageSender = event.getName();
		for (Player player : nearbyPlayers) {
			String playerName = player.getName();
			if (playerName != null && playerName.equals(messageSender)) {
				// Player chat detected within distance
				onPlayerChatDetected(player, event.getMessage(), config);
				break;
			}
		}
	}

	private static List<Player> getNearbyPlayers(WorldPoint localLocation, int maxDistance) {
		List<Player> nearbyPlayers = new ArrayList<>();
		
		for (Player player : Microbot.getClient().getTopLevelWorldView().players()) {
			if (player == null || player == Microbot.getClient().getLocalPlayer()) {
				continue;
			}
			
			WorldPoint playerLocation = player.getWorldLocation();
			if (playerLocation.distanceTo(localLocation) <= maxDistance) {
				nearbyPlayers.add(player);
			}
		}
		
		return nearbyPlayers;
	}

	public static void setFlashOverlay(QoLFlashOverlay overlay) {
		flashOverlay = overlay;
	}

	private static void onPlayerChatDetected(Player player, String message, QoLConfig config) {
		String playerName = player.getName();
		String playerNameStr = playerName != null ? playerName : "Unknown";
		String logMessage = String.format("Player nearby chat detected! Player: %s, Message: %s", 
			playerNameStr, message);
		Microbot.log(logMessage);
		
		// Flash the screen if enabled
		if (config.flashScreenOnPlayerChat() && flashOverlay != null) {
			flashOverlay.startFlash(config.playerChatFlashColor());
		}
		
		// You can add additional actions here, such as:
		// - Logging to a file
		// - Triggering an alert
		// - Taking a screenshot
		// - Sending a notification
	}
}

