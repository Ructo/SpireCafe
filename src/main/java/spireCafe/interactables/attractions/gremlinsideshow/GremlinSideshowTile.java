package spireCafe.interactables.attractions.gremlinsideshow;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;

public class GremlinSideshowTile {
    private final int x, y;
    private boolean isLit;
    private final Hitbox hitbox; // Use Hitbox for interaction
    private final String type; // Tile type

    public GremlinSideshowTile(int x, int y, boolean isLit, String type) {
        this.x = x;
        this.y = y;
        this.isLit = isLit;
        this.type = type;

        float size = 100f * com.megacrit.cardcrawl.core.Settings.scale; // Tile size scaled
        float posX = 400f + x * size; // X-coordinate for tile
        float posY = 400f + y * size; // Y-coordinate for tile
        this.hitbox = new Hitbox(posX, posY, size, size); // Initialize Hitbox
    }

    public void render(SpriteBatch sb, Texture texture) {
        sb.setColor(isLit ? Color.WHITE : Color.DARK_GRAY); // Change color based on state
        sb.draw(texture, hitbox.x, hitbox.y, hitbox.width, hitbox.height); // Render tile texture
        hitbox.render(sb); // Render hitbox for debugging
    }

    public void update() {
        hitbox.update(); // Update the hitbox
        if (hitbox.hovered && InputHelper.justClickedLeft) {
            toggle(); // Toggle tile state on click
        }
    }

    public void toggle() {
        isLit = !isLit; // Change the tile state
    }

    public boolean isLit() {
        return isLit;
    }

    public String getType() {
        return type;
    }

    public int getValue() {
        switch (type) {
            case "Chest": return 2;
            case "Money": return 3;
            case "Campfire": return 1;
            case "Monster": return -1;
            case "Elite": return -3;
            default: return 0;
        }
    }

    public Hitbox getHitbox() {
        return hitbox;
    }
}
