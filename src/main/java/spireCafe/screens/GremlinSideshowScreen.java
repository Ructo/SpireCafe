package spireCafe.screens;

import basemod.abstracts.CustomScreen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import spireCafe.Anniv7Mod;
import spireCafe.interactables.attractions.gremlinsideshow.GremlinSideshowTile;
import spireCafe.util.TexLoader;

public class GremlinSideshowScreen extends CustomScreen {

    private static final int GRID_SIZE = 5; // Grid is 5x5
    private static final int TILE_SIZE = 100; // Tile size in pixels
    private static final int MAX_CLICKS = 12; // Maximum clicks allowed

    private final GremlinSideshowTile[][] grid = new GremlinSideshowTile[GRID_SIZE][GRID_SIZE]; // Grid of tiles
    private int remainingClicks = MAX_CLICKS; // Click counter
    private boolean gameOver = false; // Game over flag
    private Texture ChestButtonTexture;
    private Texture GoldButtonTexture;
    private Texture CampfireButtonTexture;
    private Texture MonsterButtonTexture;
    private Texture EliteButtonTexture;
    private Texture EndButtonTexture;
    private Texture highlightButtonTexture;
    private Texture backgroundTexture;
    private Texture backgroundScoreTexture;

    public GremlinSideshowScreen() {
        initializeGrid();

        ChestButtonTexture = TexLoader.getTexture(Anniv7Mod.makeAttractionPath("gremlinsideshow/ChestButton.png"));
        GoldButtonTexture = TexLoader.getTexture(Anniv7Mod.makeAttractionPath("gremlinsideshow/MoneyButton.png"));
        CampfireButtonTexture = TexLoader.getTexture(Anniv7Mod.makeAttractionPath("gremlinsideshow/CampfireButton.png"));
        MonsterButtonTexture = TexLoader.getTexture(Anniv7Mod.makeAttractionPath("gremlinsideshow/MonsterButton.png"));
        EliteButtonTexture = TexLoader.getTexture(Anniv7Mod.makeAttractionPath("gremlinsideshow/EliteButton.png"));
        EndButtonTexture = TexLoader.getTexture("images/ui/leaderboards/time.png");
        highlightButtonTexture = TexLoader.getTexture("images/ui/charSelect/highlightButton2.png");
        backgroundTexture = TexLoader.getTexture("images/ui/option/confirm.png");
        backgroundScoreTexture = TexLoader.getTexture("images/ui/profile/save_panel.png");
    }
    // Initialize the grid with limits for specific tiles
    private void initializeGrid() {
        String[] tileTypes = {"Chest", "Money", "Campfire", "Monster", "Elite"};
        int chestCount = 0, monsterCount = 0, eliteCount = 0;

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                boolean isLit = Math.random() < 0.3; // Randomly lit
                String randomType;

                // Ensure limits for Chest, Monster, and Elite
                do {
                    randomType = tileTypes[(int) (Math.random() * tileTypes.length)];
                } while ((randomType.equals("Chest") && chestCount >= 3) ||
                        (randomType.equals("Monster") && monsterCount >= 3) ||
                        (randomType.equals("Elite") && eliteCount >= 3));

                // Increment counts for limited types
                if (randomType.equals("Chest")) chestCount++;
                if (randomType.equals("Monster")) monsterCount++;
                if (randomType.equals("Elite")) eliteCount++;

                grid[x][y] = new GremlinSideshowTile(x, y, isLit, randomType);
            }
        }
    }
    // Return the correct texture for each tile type
    private Texture getTextureForTile(String type) {
        switch (type) {
            case "Chest": return ChestButtonTexture;
            case "Money": return GoldButtonTexture;
            case "Campfire": return CampfireButtonTexture;
            case "Monster": return MonsterButtonTexture;
            case "Elite": return EliteButtonTexture;
            default: return null;
        }
    }
    private void toggleAdjacentTiles(int x, int y) {
        if (x > 0) {
            grid[x - 1][y].toggle();
        }
        if (x < GRID_SIZE - 1) {
            grid[x + 1][y].toggle();
        }
        if (y > 0) {
            grid[x][y - 1].toggle();
        }
        if (y < GRID_SIZE - 1) {
            grid[x][y + 1].toggle();
        }

        // Always toggle the clicked tile itself
        grid[x][y].toggle();
    }
    private void handleTileClick(int x, int y) {
        if (remainingClicks <= 0) return;

        grid[x][y].toggle(); // Toggle the clicked tile
        toggleAdjacentTiles(x, y); // Toggle adjacent tiles
        remainingClicks--;

        if (remainingClicks <= 0) {
            calculateRewards(); // Calculate rewards after clicks are exhausted
            gameOver = true;
        }
    }
    @Override
    public AbstractDungeon.CurrentScreen curScreen() {
        return ScreenEnum.GREMLIN_SIDESHOW_SCREEN; // Define this enum in your mod
    }

    public void open() {
        AbstractDungeon.screen = curScreen();
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.overlayMenu.cancelButton.show("Exit Glow Up");
        AbstractDungeon.overlayMenu.hideBlackScreen();

        // Reset game state if necessary
        if (gameOver) {
            gameOver = false;
            remainingClicks = MAX_CLICKS;
            initializeGrid();
        }
    }

    @Override
    public void reopen() {
        open(); // Reopen simply calls open
    }

    @Override
    public void close() {
        AbstractDungeon.screen = AbstractDungeon.previousScreen != null
                ? AbstractDungeon.previousScreen
                : AbstractDungeon.CurrentScreen.NONE;
        AbstractDungeon.isScreenUp = false;
        AbstractDungeon.overlayMenu.hideBlackScreen();
    }


    @Override
    public void openingSettings() {
        AbstractDungeon.previousScreen = curScreen();
    }



    @Override
    public void update() {
        if (!gameOver) {
            updateEndGameButton();
            updateGrid();
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if (gameOver) {
            renderGameOver(sb);

        } else {
            renderBackground(sb);
            renderGrid(sb);
            renderEndButton(sb);
            renderLegend(sb);
            renderUI(sb);
        }
    }
    private void renderBackground(SpriteBatch sb) {
        // Amber background (slightly larger)
        float amberBackgroundWidth = (GRID_SIZE * TILE_SIZE + 70f) * Settings.scale; // Larger than the regular background
        float amberBackgroundHeight = (GRID_SIZE * TILE_SIZE + 100f) * Settings.scale; // Larger height for emphasis
        float amberBackgroundX = 400f * Settings.scale - 35f * Settings.scale; // Slightly offset for centering
        float amberBackgroundY = 400f * Settings.scale - 60f * Settings.scale; // Slightly lower for alignment

        sb.setColor(new Color(1.0f, 0.7f, 0.3f, 1.0f)); // Amber color
        sb.draw(backgroundTexture, amberBackgroundX, amberBackgroundY, amberBackgroundWidth, amberBackgroundHeight);

        // Regular background
        float backgroundWidth = (GRID_SIZE * TILE_SIZE + 50f) * Settings.scale; // Original size
        float backgroundHeight = (GRID_SIZE * TILE_SIZE + 80f) * Settings.scale; // Original height
        float backgroundX = 400f * Settings.scale - 25f * Settings.scale; // Original position
        float backgroundY = 400f * Settings.scale - 50f * Settings.scale; // Original position

        sb.setColor(Color.WHITE); // Reset color to white
        sb.draw(backgroundTexture, backgroundX, backgroundY, backgroundWidth, backgroundHeight);
    }

    private int hoveredX = -1;
    private int hoveredY = -1;

    private void updateGrid() {
        hoveredX = -1; // Reset hovered coordinates
        hoveredY = -1;

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                GremlinSideshowTile tile = grid[x][y];
                tile.update(); // Update each tile's hitbox and state

                // Check if the current tile is hovered
                if (tile.getHitbox().hovered) {
                    hoveredX = x;
                    hoveredY = y;
                    if (InputHelper.justClickedLeft) {
                        handleTileClick(x, y);
                        return; // Exit immediately after a tile click
                    }
                }
            }
        }
    }
    private void renderGrid(SpriteBatch sb) {
        // First, render highlights for hovered or adjacent tiles
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                GremlinSideshowTile tile = grid[x][y];

                // Check if the tile is hovered or adjacent to the hovered tile
                boolean isHoveredOrAdjacent =
                        (x == hoveredX && y == hoveredY) || // Hovered tile
                                (x == hoveredX - 1 && y == hoveredY) || // Left
                                (x == hoveredX + 1 && y == hoveredY) || // Right
                                (x == hoveredX && y == hoveredY - 1) || // Below
                                (x == hoveredX && y == hoveredY + 1);   // Above

                // Draw the highlight texture if the tile is hovered or adjacent
                if (isHoveredOrAdjacent) {
                    // Calculate the position and size of the highlight
                    float highlightX = tile.getHitbox().x - 1f * Settings.scale; // Slight offset for the highlight
                    float highlightY = tile.getHitbox().y - 1f * Settings.scale;
                    float highlightWidth = tile.getHitbox().width + 2f * Settings.scale; // Expand highlight to cover the tile
                    float highlightHeight = tile.getHitbox().height + 2f * Settings.scale;

                    // Render the highlight texture behind the tile
                    sb.setColor(Color.WHITE);
                    sb.draw(highlightButtonTexture, highlightX, highlightY, highlightWidth, highlightHeight);
                }
            }
        }

        // Then, render the button textures for all tiles
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                GremlinSideshowTile tile = grid[x][y];
                Texture texture = getTextureForTile(tile.getType());
                tile.render(sb, texture); // Pass texture to the tile render method
            }
        }

        // Reset the SpriteBatch color to avoid affecting subsequent rendering
        sb.setColor(Color.WHITE);
    }

    // Add an endButton hitbox
    private final com.megacrit.cardcrawl.helpers.Hitbox endButtonHitbox = new com.megacrit.cardcrawl.helpers.Hitbox(128f * Settings.scale, 128f * Settings.scale);
    private boolean endedWithButton = false; // Tracks if the game ended via the EndButton
    private void updateEndGameButton() {
        // Position the End Game button below the grid
        float buttonX = 400f * Settings.scale; // Aligned with the grid
        float buttonY = 250f * Settings.scale; // Adjusted slightly higher

        // Update the hitbox position
        endButtonHitbox.move(buttonX + endButtonHitbox.width / 2f, buttonY + endButtonHitbox.height / 2f);
        endButtonHitbox.update();

        // Handle click logic
        if (endButtonHitbox.hovered && InputHelper.justClickedLeft) {
            endedWithButton = true; // Mark that the game ended via the button
            handleEndGame(); // End the game and calculate final rewards
        }
    }

    private void renderEndButton(SpriteBatch sb) {
        // Position the End Game button below the grid
        float buttonX = 400f * Settings.scale; // Aligned with the grid
        float buttonY = 250f * Settings.scale; // Adjusted slightly higher

        // Highlight the button if hovered
        if (endButtonHitbox.hovered) {
            // Render a slightly smaller highlight texture behind the EndButton
            float highlightSize = 100f * Settings.scale;
            sb.draw(highlightButtonTexture,
                    (buttonX + 14f) * Settings.scale,  // Centered alignment adjustment
                    (buttonY + 14f) * Settings.scale,
                    highlightSize,
                    highlightSize);
        }

        // Render the EndButton texture
        sb.setColor(Color.WHITE);
        sb.draw(EndButtonTexture, buttonX * Settings.scale, buttonY * Settings.scale, 128f * Settings.scale, 128f * Settings.scale);

        // Render the button label above the button
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
                "End Game", buttonX + 64f * Settings.scale, buttonY * Settings.scale, Color.GOLD);

        // Render the hitbox for debugging
        endButtonHitbox.render(sb);
    }
    private void renderLegend(SpriteBatch sb) {
        // Extended backgrounds behind the legend
        float legendBackgroundWidth = 300f * Settings.scale; // Wider background
        float legendBackgroundHeight = 350f * Settings.scale; // Taller background

        // Updated position for the legend background
        float legendBackgroundX = 930f * Settings.scale; // Move further to the right
        float legendBackgroundY = 366f * Settings.scale; // Move slightly down

        // Render the gold background behind the legend
        sb.setColor(new Color(1.0f, 0.7f, 0.3f, 1.0f)); // Gold color
        sb.draw(backgroundTexture, legendBackgroundX - 15f * Settings.scale, legendBackgroundY - 15f * Settings.scale,
                legendBackgroundWidth + 30f * Settings.scale, legendBackgroundHeight + 30f * Settings.scale);

        // Render the normal background behind the legend
        sb.setColor(Color.WHITE);
        sb.draw(backgroundTexture, legendBackgroundX, legendBackgroundY,
                legendBackgroundWidth, legendBackgroundHeight);

        // Adjusted position for the legend text and icons
        float legendX = legendBackgroundX + 20f * Settings.scale;
        float legendY = legendBackgroundY + legendBackgroundHeight - 40f * Settings.scale; // Adjust spacing
        float spacing = 50f * Settings.scale;  // Spacing between legend entries
        float iconSize = 40f * Settings.scale; // Slightly larger icon size

        // Render legend title
        FontHelper.renderFontLeft(sb, FontHelper.buttonLabelFont, "Legend:", legendX, legendY, Color.WHITE);
        legendY -= spacing;

        renderLegendEntry(sb, ChestButtonTexture, "Chest: +2", legendX, legendY, iconSize);
        legendY -= spacing;

        renderLegendEntry(sb, GoldButtonTexture, "Money: +3", legendX, legendY, iconSize);
        legendY -= spacing;

        renderLegendEntry(sb, CampfireButtonTexture, "Campfire: +1", legendX, legendY, iconSize);
        legendY -= spacing;

        renderLegendEntry(sb, MonsterButtonTexture, "Monster: -1", legendX, legendY, iconSize);
        legendY -= spacing;

        renderLegendEntry(sb, EliteButtonTexture, "Elite: -3", legendX, legendY, iconSize);
    }

    // Helper method to render each legend entry
    private void renderLegendEntry(SpriteBatch sb, Texture icon, String text, float x, float y, float iconSize) {
        sb.draw(icon, x, y - iconSize / 2f, iconSize, iconSize); // Draw the icon
        FontHelper.renderFontLeft(sb, FontHelper.buttonLabelFont, text, x + iconSize + 10f * Settings.scale, y, Color.WHITE);
    }
    private void renderUI(SpriteBatch sb) {
        int currentScore = 0;

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                GremlinSideshowTile tile = grid[x][y];
                if (tile.isLit()) {
                    currentScore += tile.getValue();
                }
            }
        }

        // Adjusted positions for "Clicks Remaining" and "Current Score"
        float clicksX = 45f * Settings.scale; // Moved 5f to the left
        float clicksY = 705f * Settings.scale; // Moved 5f up
        float scoreX = 45f * Settings.scale;  // Moved 5f to the left
        float scoreY = 655f * Settings.scale; // Moved 5f up

        // Adjusted background dimensions for a longer background
        float backgroundWidth = 350f * Settings.scale; // Increased by 100f
        float backgroundHeight = 50f * Settings.scale;

        // Render background for "Clicks Remaining"
        sb.setColor(Color.WHITE); // Reset color
        sb.draw(backgroundScoreTexture, clicksX - 23f * Settings.scale, clicksY - 25f * Settings.scale, backgroundWidth, backgroundHeight);

        // Render background for "Current Score"
        sb.draw(backgroundScoreTexture, scoreX - 23f * Settings.scale, scoreY - 25f * Settings.scale, backgroundWidth, backgroundHeight);

        // Render "Clicks Remaining"
        FontHelper.renderFontLeft(sb, FontHelper.buttonLabelFont,
                "Clicks Remaining: " + remainingClicks, clicksX, clicksY, Color.WHITE);

        // Render "Current Score"
        FontHelper.renderFontLeft(sb, FontHelper.buttonLabelFont,
                "Current Score: " + currentScore, scoreX, scoreY, currentScore >= 0 ? Color.GREEN : Color.RED);
    }
    private void renderGameOver(SpriteBatch sb) {
        // Render "Game Over" message at the top
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
                "Game Over!", Settings.WIDTH / 2f, Settings.HEIGHT / 2f + 100f * Settings.scale, Color.YELLOW);

        // Calculate and render the final score below "Game Over"
        int finalScore = calculateFinalScore(false); // Bonus is not included here
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
                "Final Score: " + finalScore, Settings.WIDTH / 2f, Settings.HEIGHT / 2f,
                finalScore >= 0 ? Color.GREEN : Color.RED);

        // Render a message for bonuses if the game ended with the button
        if (endedWithButton && remainingClicks > 0) {
            int bonus = (remainingClicks / 2);
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
                    "Bonus: +" + bonus + " for remaining clicks!",
                    Settings.WIDTH / 2f, Settings.HEIGHT / 2f - 50f * Settings.scale, Color.GOLD);
        }
    }

    // Refactored handleEndGame method
    private void handleEndGame() {
        // Calculate the score
        int finalScore = calculateFinalScore(endedWithButton); // Include bonus only if ended with the button

        // Apply rewards or punishment based on the final score
        if (finalScore > 0) {
            AbstractDungeon.player.gainGold(finalScore);
            CardCrawlGame.sound.play("audio/sound/SOTE_SFX_Relic_Tingsha.ogg");
            System.out.println("Final Tickets Earned (with bonus): " + finalScore);
        } else {
            applyPunishment(finalScore);
        }

        // Mark game as over and reset clicks
        gameOver = true;
        remainingClicks = 0; // Reset clicks regardless of how the game ended
        endedWithButton = false; // Reset the flag for future games
    }

    // Refactored method to calculate the final score, including bonuses
    private int calculateFinalScore(boolean includeBonus) {
        int finalScore = 0;

        // Calculate the base score
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                GremlinSideshowTile tile = grid[x][y];
                if (tile.isLit()) {
                    finalScore += tile.getValue();
                }
            }
        }

        // Add bonus if applicable
        if (includeBonus && remainingClicks > 0) {
            int bonus = remainingClicks / 2;
            finalScore += bonus;
        }

        return finalScore;
    }

    // Unified method to handle rewards and punishments
    private void handleRewardsOrPunishments(int finalScore) {
        if (finalScore > 0) {
            AbstractDungeon.player.gainGold(finalScore);
            CardCrawlGame.sound.play("SOTE_SFX_Relic_Tingsha");
            System.out.println("Final Tickets Earned: " + finalScore);
        } else {
            applyPunishment(finalScore);
        }
    }


    // Refactored calculateRewards method
    private void calculateRewards() {
        int finalScore = calculateFinalScore(false); // No bonuses during normal rewards calculation
        System.out.println("Final Score (before bonuses): " + finalScore);
        handleRewardsOrPunishments(finalScore);

        gameOver = true; // Mark game as over
    }

    // Unchanged punishment logic
    private void applyPunishment(int score) {
        CardCrawlGame.sound.play("BLUNT_FAST"); // Example punishment sound
        if (Math.random() < 0.5) {
            AbstractDungeon.player.damage(new DamageInfo(null, Math.abs(score), DamageInfo.DamageType.THORNS));
            System.out.println("Player took damage: " + Math.abs(score));
        } else {
            AbstractDungeon.topLevelEffects.add(new ShowCardAndObtainEffect(
                    AbstractDungeon.returnRandomCurse(), Settings.WIDTH / 2f, Settings.HEIGHT / 2f
            ));
            System.out.println("Player received a curse.");
        }
    }

    public static class ScreenEnum {
        @SpireEnum
        public static AbstractDungeon.CurrentScreen GREMLIN_SIDESHOW_SCREEN;
    }
}