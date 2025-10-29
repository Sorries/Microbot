package net.runelite.client.plugins.microbot.qualityoflife.scripts;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.Notifier;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.qualityoflife.QoLConfig;
import net.runelite.client.plugins.microbot.qualityoflife.QoLFlashOverlay;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerChatDetectionScript {
	private static QoLFlashOverlay flashOverlay;
	private static Notifier notifier;

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
		//Microbot.log("sender: "+ messageSender);
		for (Player player : nearbyPlayers) {
			String playerName = player.getName();
			if (playerName != null && messageSender != null) {
				// Normalize spaces (handle non-breaking spaces vs ASCII spaces)
				String normalizedPlayerName = normalizeSpaces(playerName);
				String normalizedSenderName = normalizeSpaces(messageSender);
				//if (normalizedPlayerName.equalsIgnoreCase(normalizedSenderName)) {
				if (normalizedSenderName.contains(normalizedPlayerName)) {
					// Player chat detected within distance
					onPlayerChatDetected(player, event.getMessage(), config);
					break;
				}
			}
		}
	}

	private static String normalizeSpaces(String name) {
		if (name == null) return "";
		// Replace all Unicode space characters (including non-breaking spaces) with regular ASCII spaces
		return name.replace('\u00A0', ' ').trim();
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

	public static void setNotifier(Notifier notifierInstance) {
		notifier = notifierInstance;
	}

	private static void onPlayerChatDetected(Player player, String message, QoLConfig config) {
		String playerName = player.getName();
		String playerNameStr = playerName != null ? playerName : "Unknown";
		String playerDistance = "" + player.getWorldLocation().distanceTo(Rs2Player.getWorldLocation());
		String logMessage = String.format("Player nearby chat detected! Player: %s, Message: %s, Distance: %s",
				playerNameStr, message, playerDistance);
		Microbot.log(logMessage);
		
		// Flash the screen if enabled
		if (config.flashScreenOnPlayerChat() && flashOverlay != null) {
			flashOverlay.startFlash(config.playerChatFlashColor());
			if (notifier != null ) {
				String notificationMessage = String.format("Player %s nearby: %s", playerNameStr, message);
				// Create notification without game message to avoid printing in chat box
				notifier.notify(notificationMessage);
			}
//			if (notifier != null) {
//				String notificationMessage = String.format("Player %s nearby: %s", playerNameStr, message);
//				notifier.notify(Notification.ON,notificationMessage);
//			}

		}
		
		// You can add additional actions here, such as:
		// - Logging to a file
		// - Taking a screenshot
	}

}

