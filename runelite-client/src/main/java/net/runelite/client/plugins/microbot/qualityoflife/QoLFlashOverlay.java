package net.runelite.client.plugins.microbot.qualityoflife;

import java.awt.*;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class QoLFlashOverlay extends Overlay {
	private Color flashColor = new Color(0, 0, 0, 0); // Start with fully transparent color
	private Color configuredFlashColor = new Color(255, 0, 0, 150); // Default red
	private long flashStartTime = 0;
	private static final long FLASH_DURATION_MS = 3000; // 3 seconds
	private boolean isFlashing = false;

	public QoLFlashOverlay() {
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setNaughty();
	}

	public void setFlashColor(Color newColor) {
		this.flashColor = newColor;
	}

	public void startFlash(Color flashColor) {
		this.configuredFlashColor = flashColor;
		this.flashStartTime = System.currentTimeMillis();
		this.isFlashing = true;
	}

	public Color getFlashColor() {
		return this.flashColor;
	}

	@Override
	public Dimension render(Graphics2D g) {
		if (isFlashing) {
			long currentTime = System.currentTimeMillis();
			long timeSinceStart = currentTime - flashStartTime;
			
			// Check if we should stop flashing
			if (timeSinceStart >= FLASH_DURATION_MS) {
				isFlashing = false;
				flashColor = new Color(0, 0, 0, 0);
				return null;
			}
			
			// Pulse on/off based on game cycle
			// Alternates every 200ms (flash on for 200ms, off for 200ms)
			boolean shouldShow = (timeSinceStart / 200) % 2 == 0;
			
			if (shouldShow) {
				flashColor = configuredFlashColor;
			} else {
				flashColor = new Color(0, 0, 0, 0);
			}
		}
		
		if (flashColor.getAlpha() > 0) { // Only draw if not fully transparent
			g.setColor(flashColor);
			g.fillRect(0, 0, Microbot.getClient().getCanvasWidth(), Microbot.getClient().getCanvasHeight()); // Cover the entire screen
		}
		return null;
	}
}

