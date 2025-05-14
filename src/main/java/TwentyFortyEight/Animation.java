package TwentyFortyEight;

import processing.core.PApplet;
import processing.core.PImage;

public class Animation {
    private float startX, startY, endX, endY;
    private int value;
    private float progress; // Progress of the animation from 0.0 to 1.0

    public Animation(float startX, float startY, float endX, float endY, int value) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.value = value;
        this.progress = 0.0f; // Initialize progress to 0 as the animation starts
    }

    public boolean update() {
        progress += 0.2f; // Increment by 10% each update
        return progress >= 1.0f; // Whether the animation is complete
    }

    public void draw(App app) {
        // Calculate the current position based on progress
        // Use lerp to determine the current position: start + (end - start) * progress
        float currentX = PApplet.lerp(startX, endX, progress);
        float currentY = PApplet.lerp(startY, endY, progress);
        // Get the sprite name based on the value
        String spriteName = "tile_" + value;
        PImage tile = app.getSprite(spriteName);
        // Draw the tile at current position
        app.image(tile, currentX, currentY, App.CELLSIZE, App.CELLSIZE);
    }
}

