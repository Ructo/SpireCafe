package spireCafe.screens;

import basemod.abstracts.CustomScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.ui.buttons.LargeDialogOptionButton;
import java.util.function.Consumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JukeboxScreen extends CustomScreen {
    private static final String BACKGROUND_PATH = "anniv7Resources/images/attractions/jukebox/Background.png";
    private static final String BUTTON_PATH = "anniv7Resources/images/attractions/jukebox/Button.png";
    private static final String BUTTONGLOW_PATH = "anniv7Resources/images/attractions/jukebox/ButtonGlow.png";
    private static final String BUTTONHOVER_PATH = "anniv7Resources/images/attractions/jukebox/ButtonHover.png";
    private static final String BUTTONLONG_PATH = "anniv7Resources/images/attractions/jukebox/ButtonLong.png";
    private static final String BUTTONON_PATH = "anniv7Resources/images/attractions/jukebox/ButtonOn.png";
    private static final String BUTTONOFF_PATH = "anniv7Resources/images/attractions/jukebox/ButtonOff.png";
    private static final String BUTTONDISABLED_PATH = "anniv7Resources/images/attractions/jukebox/ButtonflipN.png";
    private static final String WINDOW_PATH = "anniv7Resources/images/attractions/jukebox/Window.png";
    private static final String CUSTOM_MUSIC_FOLDER = "audio/music/custom";
    private static final int BUTTONS_PER_PAGE = 5;

    private final Texture buttonDisabledTexture;

    private final Texture backgroundTexture;
    private final Texture buttonTexture;
    private final Texture buttonglowTexture;
    private final Texture buttonhoverTexture;
    private final Texture buttonlongTexture;
    private final Texture buttononTexture;
    private final Texture buttonoffTexture;
    private final Texture windowTexture;

    private final List<LargeDialogOptionButton> buttons = new ArrayList<>();
    private final List<String> predefinedTracks = new ArrayList<>();
    private final List<String> customTracks = new ArrayList<>();
    private final List<String> allTracks = new ArrayList<>();
    private final List<Boolean> buttonClickedStates = new ArrayList<>();
    private final LargeDialogOptionButton nextButton = new LargeDialogOptionButton(999, "Next");
    private final LargeDialogOptionButton previousButton = new LargeDialogOptionButton(998, "Previous");
    public static boolean isPlaying = false;

    private int currentPage = 0;
    private static Music nowPlayingSong = null;
    private String textField = "Waiting for Song";
    private int glowingButtonIndex = -1;
    public static boolean overrideEnabled = false;
    public static boolean shopOverride = false;
    public static boolean shrineOverride = false;
    public static boolean bossOverride = false;
    public static boolean eliteOverride = false;
    public static boolean eventOverride = false;
    private Preferences preferences = Gdx.app.getPreferences("jukeboxOverrides");

    public JukeboxScreen() {
        backgroundTexture = new Texture(BACKGROUND_PATH);
        buttonTexture = new Texture(BUTTON_PATH);
        buttonglowTexture = new Texture(BUTTONGLOW_PATH);
        buttonhoverTexture = new Texture(BUTTONHOVER_PATH);
        buttonlongTexture = new Texture(BUTTONLONG_PATH);
        buttononTexture = new Texture(BUTTONON_PATH);
        buttonoffTexture = new Texture(BUTTONOFF_PATH);
        windowTexture = new Texture(WINDOW_PATH);
        buttonDisabledTexture = new Texture(BUTTONDISABLED_PATH);


        initializePredefinedTracks();
        loadCustomTracks();
        combineTracks();
        createButtons();
        initializeOverrides();
    }
    private void initializeOverrides() {
        overrideEnabled = preferences.getBoolean("overrideEnabled", false);
        shopOverride = preferences.getBoolean("shopOverride", false);
        shrineOverride = preferences.getBoolean("shrineOverride", false);
        bossOverride = preferences.getBoolean("bossOverride", false);
        eliteOverride = preferences.getBoolean("eliteOverride", false);
        eventOverride = preferences.getBoolean("eventOverride", false);
    }
    private void saveOverrides() {
        preferences.putBoolean("overrideEnabled", overrideEnabled);
        preferences.putBoolean("shopOverride", shopOverride);
        preferences.putBoolean("shrineOverride", shrineOverride);
        preferences.putBoolean("bossOverride", bossOverride);
        preferences.putBoolean("eliteOverride", eliteOverride);
        preferences.putBoolean("eventOverride", eventOverride);
        preferences.flush();
    }
    private void initializePredefinedTracks() {
        predefinedTracks.add("SHOP");
        predefinedTracks.add("SHRINE");
        predefinedTracks.add("MINDBLOOM");
        predefinedTracks.add("BOSS_BOTTOM");
        predefinedTracks.add("BOSS_CITY");
        predefinedTracks.add("BOSS_BEYOND");
        predefinedTracks.add("BOSS_ENDING");
        predefinedTracks.add("ELITE");
        predefinedTracks.add("CREDITS");
    }
    private void loadCustomTracks() {
        customTracks.clear();
        File customFolder = new File(CUSTOM_MUSIC_FOLDER);
        if (!customFolder.exists()) {
            customFolder.mkdirs();
        }

        File[] files = customFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".ogg")) {
                    customTracks.add(file.getName().replace(".ogg", ""));
                }
            }
        }
    }
    private void combineTracks() {
        allTracks.clear();
        allTracks.addAll(predefinedTracks);
        allTracks.addAll(customTracks);
    }

    private void createButtons() {
        buttons.clear();
        buttonClickedStates.clear();
        int startIndex = currentPage * BUTTONS_PER_PAGE;
        int endIndex = Math.min(startIndex + BUTTONS_PER_PAGE, allTracks.size());

        for (int i = startIndex; i < endIndex; i++) {
            buttons.add(new LargeDialogOptionButton(i, allTracks.get(i)));
            buttonClickedStates.add(false); // Initially, no button is clicked
        }
    }

    public void open() {
        AbstractDungeon.screen = curScreen();
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.overlayMenu.cancelButton.show("Close Jukebox");
        AbstractDungeon.overlayMenu.hideBlackScreen();

        if (nowPlayingSong != null) {
            textField = "Now Playing: " + nowPlayingSong;
            glowingButtonIndex = allTracks.indexOf(nowPlayingSong);
        } else {
            textField = "Waiting for Song";
            glowingButtonIndex = -1;
        }
    }

    @Override
    public void reopen() {
        open();
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
    public AbstractDungeon.CurrentScreen curScreen() {
        return ScreenEnum.JUKEBOX_SCREEN;
    }
    @Override
    public void update() {
        float mouseX = InputHelper.mX;
        float mouseY = InputHelper.mY;
        float horizontalRadius = 165f * Settings.scale; // Wider horizontal radius
        float verticalRadius = 43f * Settings.scale; // Smaller vertical radius
        // Check toggle buttons
        float baseX = 530f; // Starting X position
        float baseY = 250f; // Y position for buttons
        float horizontalSpacing = 160f; // Spacing between buttons
        float buttonWidth = 64f * Settings.scale; // Button width
        float buttonHeight = 42f * Settings.scale; // Button height

        Consumer<Boolean>[] actions = new Consumer[]{
                (value) -> overrideEnabled = (boolean) value,
                (value) -> shopOverride = (boolean) value,
                (value) -> shrineOverride = (boolean) value,
                (value) -> bossOverride = (boolean) value,
                (value) -> eliteOverride = (boolean) value,
                (value) -> eventOverride = (boolean) value
        };

        boolean[] states = {overrideEnabled, shopOverride, shrineOverride, bossOverride, eliteOverride, eventOverride};
        String[] labels = {"Override", "Shop Music", "Shrine Music", "Boss Music", "Elite Music", "Event Music"};

        for (int i = 0; i < labels.length; i++) {
            float buttonX = baseX + (i * horizontalSpacing);
            float buttonY = baseY;

            if (InputHelper.justClickedLeft && isMouseWithinBounds(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight)) {
                System.out.println("Button clicked: " + labels[i]); // Debug log
                actions[i].accept(!states[i]); // Toggle state
                saveOverrides(); // Save updated state
                break; // Prevent other buttons from being toggled
            }
        }

        for (int i = 0; i < buttons.size(); i++) {
            LargeDialogOptionButton button = buttons.get(i);

            float x = 408f * Settings.scale; // Button X position
            float y = (613f - (i * 85f)) * Settings.scale; // Button Y position
            float centerX = x + (200f * Settings.scale); // Center X of button
            float centerY = y + (100f * Settings.scale); // Center Y of button

            // Check if the mouse is within the horizontal and vertical bounds of the ellipse
            float horizontalDist = Math.abs(mouseX - centerX) / horizontalRadius;
            float verticalDist = Math.abs(mouseY - centerY) / verticalRadius;
            boolean isHovered = (horizontalDist * horizontalDist) + (verticalDist * verticalDist) <= 1;

            if (isHovered && InputHelper.justClickedLeft) {
                // Mark the clicked button and reset others
                for (int j = 0; j < buttonClickedStates.size(); j++) {
                    buttonClickedStates.set(j, false); // Reset all to false
                }
                buttonClickedStates.set(i, true); // Set the clicked button to true
                handleButtonClick(button); // Trigger click behavior
            }
        }
    }
    @Override
    public void render(SpriteBatch sb) {
        sb.setColor(Color.WHITE);
        sb.draw(backgroundTexture, 0, 0, Settings.WIDTH, Settings.HEIGHT);

        float mouseX = InputHelper.mX;
        float mouseY = InputHelper.mY;
        float horizontalRadius = 165f * Settings.scale; // Wider horizontal radius
        float verticalRadius = 43f * Settings.scale; // Smaller vertical radius

        for (int i = 0; i < buttons.size(); i++) {
            LargeDialogOptionButton button = buttons.get(i);

            float x = 408f * Settings.scale;
            float y = (613f - (i * 80f)) * Settings.scale;
            float centerX = x + 200f; // Center X of button
            float centerY = y + 113f; // Center Y of button

            // Draw the base button texture
            sb.draw(buttonTexture, x, y, 400f * Settings.scale, 200f * Settings.scale);

            // Render hover and glow textures as overlays
            if (buttonClickedStates.get(i)) {
                sb.draw(buttonglowTexture, x, y, 400f * Settings.scale, 200f * Settings.scale);
            } else if (isMouseWithinBounds(mouseX, mouseY, centerX, centerY, horizontalRadius, verticalRadius)) {
                sb.draw(buttonhoverTexture, x, y, 400f * Settings.scale, 200f * Settings.scale);
            }

            // Render the button text
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, button.msg, x + (199f * Settings.scale), y + (99f * Settings.scale), Color.WHITE);
        }
        float windowX = (Settings.WIDTH / 2f);
        float windowY = (Settings.HEIGHT / 2f);
        sb.draw(windowTexture, windowX-450f, windowY+170f, 900f * Settings.scale, 250f * Settings.scale);
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, textField, Settings.WIDTH / 2f, Settings.HEIGHT - (242f * Settings.scale), Color.GOLD);

        renderPaginationButtons(sb);
        addOverrideSettingsToJukebox(sb);
    }
    private boolean isMouseWithinBounds(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y - height && mouseY <= y;
    }


    private void renderPaginationButtons(SpriteBatch sb) {
        float nextX = 1208f * Settings.scale;
        float nextY = 100f * Settings.scale;
        renderPaginationButton(nextX, nextY, buttonlongTexture, "Next", sb);

        float prevX = 508f * Settings.scale;
        float prevY = 100f * Settings.scale;
        renderPaginationButton(prevX, prevY, buttonlongTexture, "Previous", sb);
    }

    private void renderPaginationButton(float x, float y, Texture texture, String label, SpriteBatch sb) {
        sb.draw(texture, (x - 50f) * Settings.scale, y, 300f * Settings.scale, 85f * Settings.scale);
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, label,
                (x + 100f) * Settings.scale, (y + 40f) * Settings.scale, Color.WHITE);
    }
    private void addOverrideSettingsToJukebox(SpriteBatch sb) {

        // Initial position for the buttons and text
        float baseX = 530.0f; // Starting X position for the first button
        float baseY = 250.0f; // Y position for both buttons and text
        float horizontalSpacing = 160.0f; // Spacing between buttons
// Render each button with its corresponding text
        renderOverrideOption(sb, "Override Music", baseX - 10f * Settings.scale, baseY - 30f * Settings.scale, overrideEnabled, false, (value) -> {
            overrideEnabled = value; // Update overrideEnabled
            if (!overrideEnabled) {
                // Automatically disable all dependent buttons
                shopOverride = false;
                shrineOverride = false;
                bossOverride = false;
                eliteOverride = false;
                eventOverride = false;
            }
            saveOverrides();
        });

        renderOverrideOption(sb, "Shop", baseX + horizontalSpacing - 10f * Settings.scale, baseY - 30f * Settings.scale, shopOverride, overrideEnabled, (value) -> {
            if (overrideEnabled) {
                shopOverride = value;
                saveOverrides();
            }
        });

        renderOverrideOption(sb, "Shrine", baseX + (2 * horizontalSpacing) - 10f * Settings.scale, baseY - 30f * Settings.scale, shrineOverride, overrideEnabled, (value) -> {
            if (overrideEnabled) {
                shrineOverride = value;
                saveOverrides();
            }
        });

        renderOverrideOption(sb, "Boss", baseX + (3 * horizontalSpacing) - 10f * Settings.scale, baseY - 30f * Settings.scale, bossOverride, overrideEnabled, (value) -> {
            if (overrideEnabled) {
                bossOverride = value;
                saveOverrides();
            }
        });

        renderOverrideOption(sb, "Elite", baseX + (4 * horizontalSpacing) - 10f * Settings.scale, baseY - 30f * Settings.scale, eliteOverride, overrideEnabled, (value) -> {
            if (overrideEnabled) {
                eliteOverride = value;
                saveOverrides();
            }
        });

        renderOverrideOption(sb, "Event", baseX + (5 * horizontalSpacing) - 10f * Settings.scale, baseY - 30f * Settings.scale, eventOverride, overrideEnabled, (value) -> {
            if (overrideEnabled) {
                eventOverride = value;
                saveOverrides();
            }
        });
    }

    private void renderOverrideOption(SpriteBatch sb, String label, float buttonX, float buttonY, boolean isActive, boolean isParentEnabled, Consumer<Boolean> onClick) {
        float buttonWidth = 64f * Settings.scale;
        float buttonHeight = 42f * Settings.scale;
        boolean isDisabled = !isParentEnabled && !label.equals("Override Music");

        Texture currentTexture = isDisabled ? buttonDisabledTexture : (isActive ? buttononTexture : buttonoffTexture);
        sb.draw(currentTexture, buttonX, buttonY, buttonWidth, buttonHeight);

        if (!isDisabled && InputHelper.justClickedLeft && isMouseWithinBounds(InputHelper.mX, InputHelper.mY, buttonX, buttonY, buttonWidth, buttonHeight)) {
            onClick.accept(!isActive);
        }

        FontHelper.renderFontCentered(sb, FontHelper.charDescFont, label, buttonX + (buttonWidth / 2f), buttonY + (buttonHeight + 20f), Settings.CREAM_COLOR);
    }


    @Override
    public void openingSettings() {
        AbstractDungeon.previousScreen = curScreen();
    }

    @Override
    public boolean allowOpenDeck() {
        return true;
    }

    @Override
    public void openingDeck() {
        AbstractDungeon.previousScreen = curScreen();
    }

    @Override
    public boolean allowOpenMap() {
        return false;
    }
    private void handleButtonClick(LargeDialogOptionButton button) {
        String selectedTrack = allTracks.get(button.slot);
        textField = "Now Playing: " + selectedTrack;

        if (predefinedTracks.contains(selectedTrack)) {
            // Use CardCrawlGame.music for predefined tracks
            String trackFileName = getTrackFileName(selectedTrack);
            System.out.println("Playing predefined track: " + trackFileName);
            stopCurrentMusic();
            CardCrawlGame.music.playTempBgmInstantly(trackFileName, true); // Pass only the file name
            isPlaying = true;
        } else {
            // Use Gdx.audio for custom tracks
            String fullPath = Gdx.files.absolute(CUSTOM_MUSIC_FOLDER + "/" + selectedTrack + ".ogg").path();
            System.out.println("Trying to load custom music file at: " + fullPath);
            playTempBgm(fullPath); // Play custom music
        }
    }
    private String getTrackFileName(String key) {
        switch (key) {
            case "SHOP": return "STS_Merchant_NewMix_v1.ogg";
            case "SHRINE": return "STS_Shrine_NewMix_v1.ogg";
            case "MINDBLOOM": return "STS_Boss1MindBloom_v1.ogg";
            case "BOSS_BOTTOM": return "STS_Boss1_NewMix_v1.ogg";
            case "BOSS_CITY": return "STS_Boss2_NewMix_v1.ogg";
            case "BOSS_BEYOND": return "STS_Boss3_NewMix_v1.ogg";
            case "BOSS_ENDING": return "STS_Boss4_v6.ogg";
            case "ELITE": return "STS_EliteBoss_NewMix_v1.ogg";
            case "CREDITS": return "STS_Credits_v5.ogg";
            default: throw new IllegalArgumentException("Unknown track key: " + key);
        }
    }
    public static void playTempBgm(String path) {
        try {
            stopCurrentMusic(); // Stop any currently playing music

            FileHandle fileHandle = Gdx.files.absolute(path);
            if (!fileHandle.exists()) {
                System.out.println("Custom music file not found: " + path);
                return;
            }

            nowPlayingSong = Gdx.audio.newMusic(fileHandle);
            nowPlayingSong.setLooping(true); // Optional: Set looping for custom tracks
            nowPlayingSong.setVolume(Settings.MUSIC_VOLUME); // Adjust volume based on game settings
            nowPlayingSong.play();
        } catch (Exception e) {
            System.err.println("Failed to play custom music: " + path);
            e.printStackTrace();
            stopCurrentMusic();
        }
    }
    public static void stopCurrentMusic() {
        if (nowPlayingSong != null) {
            nowPlayingSong.stop();
            nowPlayingSong.dispose();
            nowPlayingSong = null;
        } else if (isPlaying) {
            CardCrawlGame.music.silenceTempBgmInstantly();
            CardCrawlGame.music.silenceBGM();
            isPlaying = false;
        }
    }

    public static class ScreenEnum {
        @SpireEnum
        public static AbstractDungeon.CurrentScreen JUKEBOX_SCREEN;
    }
}