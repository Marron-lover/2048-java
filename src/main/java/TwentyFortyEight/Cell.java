package TwentyFortyEight;
import processing.core.*;

public class Cell {
    private int gridX, gridY; // The grid position of the cell
    private int value; // The value of the cell (0 for empty, 2, 4, etc. for filled)
    private boolean hovered; // Whether the cell is hovered over
    private float pixelX, pixelY; // The pixel position of the cell on screen

    // Constructor to initialize the cell
    public Cell(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.value = 0; // Initialize with empty value
        this.hovered = false;
        this.pixelX = gridX * App.CELLSIZE;
        this.pixelY = gridY * App.CELLSIZE;
    }

    public void draw(App app) {
        String spriteName;
        if (value == 0) {
            spriteName = hovered ? "tile_hover" : "tile";
        }
        else {
            spriteName = "tile_" + value;
        }
        PImage tile = app.getSprite(spriteName);
        app.image(tile, getPixelX(), getPixelY(), App.CELLSIZE, App.CELLSIZE);
    }

    // Set the grid position and pixel position of the cell
    public void setGridPosition(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.pixelX = gridX * App.CELLSIZE;
        this.pixelY = gridY * App.CELLSIZE;
    }

    // Getters and setters
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public int getPixelX() {
        return (int) pixelX;
    }

    public int getPixelY() {
        return (int) pixelY;
    }
}

