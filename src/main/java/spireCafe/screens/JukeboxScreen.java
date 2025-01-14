package spireCafe.screens;

import basemod.abstracts.CustomScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.ConfigUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.ui.buttons.LargeDialogOptionButton;
import spireCafe.Anniv7Mod;
import spireCafe.interactables.attractions.jukebox.JukeboxRelic;
import spireCafe.interactables.patrons.missingno.MissingnoRelic;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
public class JukeboxScreen extends CustomScreen {
    private static final String CUSTOM_MUSIC_FOLDER = ConfigUtils.CONFIG_DIR + File.separator + "anniv7/cafe_jukebox";
    private static final int BUTTONS_PER_PAGE = 5;
    private final List<LargeDialogOptionButton> buttons = new ArrayList<>();
    private final List<String> predefinedTracks = new ArrayList<>();
    private final List<String> customTracks = new ArrayList<>();
    private final List<String> allTracks = new ArrayList<>();
    private final List<Boolean> buttonHoverStates = new ArrayList<>();

    private final List<Boolean> buttonClickedStates = new ArrayList<>();
    private boolean isQueueConfirmState = false; // Tracks if the button says "Confirm" or "Add to Queue"
    private List<String> queuedTracks = new ArrayList<>(); // List of selected tracks
    public static boolean isPlaying = false;
    public static boolean isCoinSlotClicked = false;

    private int currentPage = 0;
    private static Music nowPlayingSong = null;
    private boolean loopEnabled = false;
    private String currentTrackName;
    private int glowingButtonIndex = -1;
    private String textField;

    private Texture backgroundTexture;
    private Texture buttonTexture;
    private Texture buttonglowTexture;
    private Texture buttonhoverTexture;
    private Texture buttonlongTexture;
    private Texture buttononTexture;
    private Texture buttonoffTexture;
    private Texture buttonDisabledTexture;
    private Texture coinSlotTexture;
    private Texture coinSlotLitTexture;
    private Texture coinSlotThankyouTexture;
    private Texture highlightButtonTexture;
    private Texture windowTexture;
    private Texture insertButtonTexture;
    private Texture refreshButtonTexture;
    private Texture loopButtonTexture;
    private Texture loopButtonGlowTexture;
    private Texture skipButtonTexture;
    private Texture clearButtonTexture;
    private Texture clearButtonOffTexture;
    private Texture insertSlotTexture;
    private Texture insertSlotGlowTexture;
    private static final String ID = Anniv7Mod.makeID(JukeboxScreen.class.getSimpleName());
    private static final UIStrings UIStrings = CardCrawlGame.languagePack.getUIString(ID);
    private static final String[] TEXT = UIStrings.TEXT;

    public JukeboxScreen() {
        textField = TEXT[22];
        glowingButtonIndex = -1;

        backgroundTexture = new Texture(TEXT[0]);
        buttonTexture = new Texture(TEXT[1]);
        buttonglowTexture = new Texture(TEXT[2]);
        buttonhoverTexture = new Texture(TEXT[3]);
        buttonlongTexture = new Texture(TEXT[4]);
        buttononTexture = new Texture(TEXT[5]);
        buttonoffTexture = new Texture(TEXT[6]);
        buttonDisabledTexture = new Texture(TEXT[7]);
        coinSlotTexture = new Texture(TEXT[8]);
        coinSlotLitTexture = new Texture(TEXT[9]);
        coinSlotThankyouTexture = new Texture(TEXT[10]);
        highlightButtonTexture = new Texture(TEXT[11]);
        windowTexture = new Texture(TEXT[12]);
        insertButtonTexture = new Texture(TEXT[13]);
        refreshButtonTexture = new Texture(TEXT[14]);
        loopButtonTexture = new Texture(TEXT[15]);
        loopButtonGlowTexture = new Texture(TEXT[16]);
        skipButtonTexture = new Texture(TEXT[17]);
        clearButtonTexture = new Texture(TEXT[18]);
        clearButtonOffTexture = new Texture(TEXT[19]);
        insertSlotTexture = new Texture(TEXT[20]);
        insertSlotGlowTexture = new Texture(TEXT[21]);

        initializePredefinedTracks();
        loadCustomTracks();
        combineTracks();
        createButtons();

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
            System.out.println("Custom music folder not found: " + CUSTOM_MUSIC_FOLDER);
            boolean created = customFolder.mkdirs(); // Attempt to create the folder
            if (created) {
                System.out.println("Custom music folder created: " + CUSTOM_MUSIC_FOLDER);
            } else {
                System.err.println("Failed to create custom music folder.");
                return;
            }
        }

        // Ensure Cafe_Theme.mp3 is present
        File cafeThemeFile = new File(customFolder, "Cafe_Theme.mp3");
        if (!cafeThemeFile.exists()) {
            System.out.println("Cafe_Theme.mp3 not found in custom folder. Adding default file...");
            copyDefaultCafeTheme(cafeThemeFile);
        } else {
            System.out.println("Cafe_Theme.mp3 already exists in custom folder.");
        }

        // Load files from the custom folder
        File[] files = customFolder.listFiles();
        if (files != null) {
            System.out.println("Scanning custom music folder for audio files...");
            for (File file : files) {
                if (file.isFile() && isValidAudioFile(file.getName())) {
                    String trimmedName = file.getName()
                            .replaceAll("\\.(ogg|mp3|wav)$", "") // Remove extensions
                            .replace("_", " ");                 // Replace underscores with spaces
                    customTracks.add(trimmedName);
                    System.out.println("Custom track added: " + trimmedName);
                } else {
                    System.out.println("Invalid or unsupported file skipped: " + file.getName());
                }
            }
        }
    }

    // Helper method to copy Cafe_Theme.mp3 to the custom folder
    private void copyDefaultCafeTheme(File destination) {
        try {
            FileHandle sourceFile = Gdx.files.internal("anniv7Resources/audio/Cafe_Theme.mp3");
            if (sourceFile.exists()) {
                FileHandle destFile = Gdx.files.absolute(destination.getAbsolutePath());
                sourceFile.copyTo(destFile);
                System.out.println("Successfully copied Cafe_Theme.mp3 to: " + destination.getAbsolutePath());
            } else {
                System.err.println("Default Cafe_Theme.mp3 not found in resources.");
            }
        } catch (Exception e) {
            System.err.println("Failed to copy Cafe_Theme.mp3 to custom folder.");
            e.printStackTrace();
        }
    }

    // Helper method to check for valid audio file extensions
    private boolean isValidAudioFile(String fileName) {
        // Convert to lowercase to handle case-insensitive matching
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".ogg") ||
                lowerCaseName.endsWith(".mp3") ||
                lowerCaseName.endsWith(".wav");
    }

    private void combineTracks() {
        allTracks.clear();
        allTracks.addAll(predefinedTracks); // Add predefined tracks
        allTracks.addAll(customTracks);     // Add custom tracks

        System.out.println("Predefined tracks: " + predefinedTracks);
        System.out.println("Custom tracks: " + customTracks);
        System.out.println("All tracks combined: " + allTracks);
    }

    private String formatTrackName(String trackName) {
        // Remove the file extension and replace underscores with spaces
        trackName = trackName.replaceAll("\\.ogg|\\.mp3|\\.wav", "").replace("_", " ");
        // Capitalize each word
        String[] words = trackName.split(" ");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                formattedName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return formattedName.toString().trim();
    }

    private void createButtons() {
        buttons.clear();
        buttonClickedStates.clear();
        buttonHoverStates.clear(); // Initialize hover states
        // Determine the range of tracks for the current page
        int startIndex = currentPage * (BUTTONS_PER_PAGE * 2); // Two columns per page
        int endIndex = Math.min(startIndex + (BUTTONS_PER_PAGE * 2), allTracks.size());

        System.out.println("Creating buttons for page: " + currentPage + " (Tracks " + startIndex + " to " + (endIndex - 1) + ")");


        for (int i = startIndex; i < endIndex; i++) {
            String trackName = allTracks.get(i);
            buttons.add(new LargeDialogOptionButton(i, trackName));
            buttonClickedStates.add(false);
            buttonHoverStates.add(false);
        }
    }

    public void open() {
        AbstractDungeon.screen = curScreen();
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.overlayMenu.cancelButton.show(TEXT[24]); // "Close Jukebox"
        AbstractDungeon.overlayMenu.hideBlackScreen();

        if (nowPlayingSong != null) {
            String formattedTrackName = formatTrackName(nowPlayingSong.toString()); // Format the track name
            textField = TEXT[23] + formattedTrackName; // "Now Playing: [formattedTrackName]"
            glowingButtonIndex = allTracks.indexOf(formattedTrackName);
        } else {
            textField = TEXT[22];
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
        clearHighlightsOnEmptyTrack();
        updateSongButtons();
        updatePaginationButtons();
        updateCoinSlot();
        updateSideButtons();
        updateInsertLogic();
        updateAddToQueueButton();
        updateClearQueueButton();
    }


    @Override
    public void render(SpriteBatch sb) {
        renderBackground(sb);
        renderSongButtons(sb);
        renderCoinSlot(sb);
        renderPaginationButtons(sb);
        renderSideButtons(sb);
        renderInsertLogic(sb);
        renderAddToQueueButton(sb);
        renderClearQueueButton(sb);
    }

    // U&R is Update & Render as each option goes Update then Render in order below.
    private void renderBackground(SpriteBatch sb) {
        sb.setColor(Color.WHITE);

        // Draw background
        sb.draw(backgroundTexture, 0, 0, Settings.WIDTH, Settings.HEIGHT);

        // Draw window and text field
        float windowX = (Settings.WIDTH / 2f);
        float windowY = (Settings.HEIGHT / 2f);
        sb.draw(windowTexture, windowX - 450f, windowY + 170f, 900f * Settings.scale, 250f * Settings.scale);
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,
                TEXT[23]  + (currentTrackName != null ? currentTrackName : TEXT[22]),
                Settings.WIDTH / 2f, Settings.HEIGHT - (242f * Settings.scale),
                Color.GOLD);
    }

    //U&R Song Buttons
    private void updateSongButtons() {
        float leftColumnX = 408f * Settings.scale;
        float rightColumnX = 808f * Settings.scale;
        float startingY = 613f * Settings.scale;
        float buttonSpacing = 85f * Settings.scale;

        for (int i = 0; i < buttons.size(); i++) {
            LargeDialogOptionButton button = buttons.get(i);

            // Determine x and y based on the column (left or right)
            float x = (i < BUTTONS_PER_PAGE) ? leftColumnX : rightColumnX;
            float y = startingY - ((i % BUTTONS_PER_PAGE) * buttonSpacing);

            float hitboxWidth = 300f * Settings.scale;
            float hitboxHeight = 70f * Settings.scale;
            float hitboxX = x + (400f * Settings.scale - hitboxWidth) / 2f;
            float hitboxY = y + (200f * Settings.scale - hitboxHeight) / 2f;

            Hitbox buttonHitbox = new Hitbox(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
            buttonHitbox.update();

            buttonHoverStates.set(i, buttonHitbox.hovered);
            if (buttonHitbox.hovered && InputHelper.justClickedLeft) {
                // Handle click logic
                for (int j = 0; j < buttonClickedStates.size(); j++) {
                    buttonClickedStates.set(j, false);
                }
                buttonClickedStates.set(i, true);

                if (isQueueConfirmState) {
                    String selectedTrack = allTracks.get(button.slot);
                    if (queuedTracks.contains(selectedTrack)) {
                        queuedTracks.remove(selectedTrack);
                        System.out.println("Track removed from queue: " + selectedTrack);
                    } else {
                        queuedTracks.add(selectedTrack);
                        System.out.println("Track added to queue: " + selectedTrack);
                    }
                } else {
                    handleButtonClick(button);
                }
            }
        }
    }

    private void renderSongButtons(SpriteBatch sb) {
        // Positions and spacing
        float leftColumnX = 408f * Settings.scale;
        float rightColumnX = 808f * Settings.scale;
        float startingY = 613f * Settings.scale;
        float buttonSpacing = 85f * Settings.scale;

        // Iterate through the buttons and render them
        for (int i = 0; i < buttons.size(); i++) {
            LargeDialogOptionButton button = buttons.get(i);

            // Determine x and y based on the column (left or right)
            float x = (i < BUTTONS_PER_PAGE) ? leftColumnX : rightColumnX;
            float y = startingY - ((i % BUTTONS_PER_PAGE) * buttonSpacing);

            // Draw the visual button
            float visualWidth = 400f * Settings.scale;
            float visualHeight = 200f * Settings.scale;
            sb.draw(buttonTexture, x, y, visualWidth, visualHeight);

            // Highlight if clicked
            if (buttonClickedStates.get(i)) {
                sb.draw(buttonglowTexture, x, y, visualWidth, visualHeight);
            }
            // Highlight if hovered
            else if (buttonHoverStates.get(i)) {
                sb.draw(buttonhoverTexture, x, y, visualWidth, visualHeight);
            }

            // Render the formatted button text
            String displayName = formatTrackName(button.msg);
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, displayName,
                    x + (visualWidth / 2f), y + (visualHeight / 2f), Color.WHITE);

            // Render the hitbox for debugging
            float hitboxWidth = 300f * Settings.scale;
            float hitboxHeight = 70f * Settings.scale;
            float hitboxX = x + (visualWidth - hitboxWidth) / 2f;
            float hitboxY = y + (visualHeight - hitboxHeight) / 2f;
            Hitbox debugHitbox = new Hitbox(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
            debugHitbox.render(sb);
            String selectedTrack = allTracks.get(button.slot);
            if (queuedTracks.contains(selectedTrack)) {
                // Get queue number and render it
                int queuePosition = queuedTracks.indexOf(selectedTrack) + 1;
                String queueNumber = Integer.toString(queuePosition);

                // Render the queue number next to the button
                FontHelper.renderFontLeftTopAligned(sb, FontHelper.buttonLabelFont, queueNumber,
                        x + visualWidth * Settings.scale, y + visualHeight / 2f, Settings.GOLD_COLOR);
            }
        }
    }

    //U&R Side Buttons and Hitboxes for them
    private final Hitbox insertHitbox = new Hitbox(80f * Settings.scale, 80f * Settings.scale);
    private Hitbox insertSlotHitbox = new Hitbox(260f * Settings.scale, 38f * Settings.scale);
    private final Hitbox refreshHitbox = new Hitbox(80f * Settings.scale, 80f * Settings.scale);
    private final Hitbox loopHitbox = new Hitbox(80f * Settings.scale, 80f * Settings.scale);
    private final Hitbox skipButtonHitbox = new Hitbox(80f * Settings.scale, 80f * Settings.scale);


    private void updateSideButtons() {
        float sideButtonWidth = 80f * Settings.scale;
        float sideButtonHeight = 80f * Settings.scale;

        // Refresh Button
        float refreshButtonX = 1350f * Settings.scale;
        float refreshButtonY = 300f * Settings.scale;
        refreshHitbox.move(refreshButtonX + sideButtonWidth / 2f, refreshButtonY + sideButtonHeight / 2f);
        refreshHitbox.update();
        if (refreshHitbox.hovered && InputHelper.justClickedLeft) {
            System.out.println("Refresh button clicked!");
            CardCrawlGame.sound.play("UI_CLICK_1");
            refreshTrackList();
        }

        // Loop Button
        float loopButtonX = 1250f * Settings.scale;
        float loopButtonY = 300f * Settings.scale;
        loopHitbox.move(loopButtonX + sideButtonWidth / 2f, loopButtonY + sideButtonHeight / 2f);
        loopHitbox.update();
        if (loopHitbox.hovered && InputHelper.justClickedLeft) {
            System.out.println("Loop button clicked!");
            CardCrawlGame.sound.play("UI_CLICK_1");
            loopEnabled = !loopEnabled;
            updateLoopingState();
            System.out.println("Looping is now: " + (loopEnabled ? "Enabled" : "Disabled"));
        }
        //Skip Button
        float buttonX = 1250f * Settings.scale;
        float buttonY = 510f * Settings.scale;

        skipButtonHitbox.move(buttonX + skipButtonHitbox.width / 2f, buttonY + skipButtonHitbox.height / 2f);
        skipButtonHitbox.update();

        if (skipButtonHitbox.hovered && InputHelper.justClickedLeft) {
            System.out.println("Skip button clicked!");
            stopCurrentMusic();
            if (!queuedTracks.isEmpty()) {
                playQueuedTracks(); // Play the next queued track
            } else {
                playRandomTrack(); // Play a random track if the queue is empty
            }
        }
    }

    private void renderSideButtons(SpriteBatch sb) {
        float sideButtonWidth = 80f * Settings.scale;
        float sideButtonHeight = 80f * Settings.scale;
        // Refresh Button
        float refreshButtonX = 1350f * Settings.scale;
        float refreshButtonY = 300f * Settings.scale;
        if (refreshHitbox.hovered) {
            sb.draw(highlightButtonTexture, 1347f * Settings.scale, 297f * Settings.scale, 85f * Settings.scale, 85f * Settings.scale);
        }
        sb.draw(refreshButtonTexture, refreshButtonX, refreshButtonY, sideButtonWidth, sideButtonHeight);

        // Loop Button
        float loopButtonX = 1250f * Settings.scale;
        float loopButtonY = 300f * Settings.scale;
        if (loopHitbox.hovered) {
            sb.draw(highlightButtonTexture, 1247f * Settings.scale, 297f * Settings.scale, 85f * Settings.scale, 85f * Settings.scale);
        }
        if (loopEnabled) {
            sb.draw(loopButtonGlowTexture, loopButtonX, loopButtonY, sideButtonWidth, sideButtonHeight);
        } else {
            sb.draw(loopButtonTexture, loopButtonX, loopButtonY, sideButtonWidth, sideButtonHeight);
        }

        //Skip Button

        float skipbuttonX = 1250f * Settings.scale; // Position for the Skip button
        float skipbuttonY = 505f * Settings.scale; // Slightly below Clear Queue button
        if (skipButtonHitbox.hovered) {
            sb.draw(highlightButtonTexture, 1247f * Settings.scale, 502f * Settings.scale, 85f * Settings.scale, 85f * Settings.scale);
        }
        sb.draw(skipButtonTexture, skipbuttonX, skipbuttonY, sideButtonWidth, sideButtonHeight);
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, TEXT[34],
                skipbuttonX + skipButtonHitbox.width / 2f, skipbuttonY - (skipButtonHitbox.height / 5f), Color.WHITE);

        // Render hitbox for debugging

        skipButtonHitbox.render(sb);
        refreshHitbox.render(sb);
        loopHitbox.render(sb);
    }
    private void refreshTrackList() {
        System.out.println("Refreshing track list...");
        loadCustomTracks(); // Reload tracks from the custom folder
        combineTracks();    // Combine predefined and custom tracks
        createButtons();    // Refresh the button list
        System.out.println("Track list refreshed!");
    }

    //U&R Insert Button and slot
    private void updateInsertLogic() {
        // Dimensions
        float sideButtonWidth = 80f * Settings.scale;
        float sideButtonHeight = 80f * Settings.scale;
        float insertSlotWidth = 260f * Settings.scale;
        float insertSlotHeight = 38f * Settings.scale;

        // Insert Button Hitbox
        float insertButtonX = 1450f * Settings.scale;
        float insertButtonY = 300f * Settings.scale;
        insertHitbox.move(insertButtonX + sideButtonWidth / 2f, insertButtonY + sideButtonHeight / 2f);
        insertHitbox.update();

        // Insert Slot Hitbox
        float insertSlotX = 1250f * Settings.scale;
        float insertSlotY = 390f * Settings.scale;
        insertSlotHitbox.move(insertSlotX + insertSlotWidth / 2f, insertSlotY + insertSlotHeight / 2f);
        insertSlotHitbox.update();

        // Shared hover and click logic
        if ((insertHitbox.hovered || insertSlotHitbox.hovered) && InputHelper.justClickedLeft) {
            System.out.println("Insert buttons clicked!");
            CardCrawlGame.sound.play("UI_CLICK_1");
            openFileExplorer(CUSTOM_MUSIC_FOLDER);
        }
    }
    private void renderInsertLogic(SpriteBatch sb) {
        // Dimensions
        float sideButtonWidth = 80f * Settings.scale;
        float sideButtonHeight = 80f * Settings.scale;
        float insertSlotWidth = 270f * Settings.scale;
        float insertSlotHeight = 38f * Settings.scale;

        // Insert Button
        float insertButtonX = 1450f * Settings.scale;
        float insertButtonY = 300f * Settings.scale;
        if (insertHitbox.hovered || insertSlotHitbox.hovered) {
            sb.draw(highlightButtonTexture, 1447f * Settings.scale, 297f * Settings.scale, 85f * Settings.scale, 85f * Settings.scale);
        }
        sb.draw(insertButtonTexture, insertButtonX, insertButtonY, sideButtonWidth, sideButtonHeight);

        // Insert Slot
        float insertSlotX = 1255f * Settings.scale;
        float insertSlotY = 390f * Settings.scale;
        if (insertHitbox.hovered || insertSlotHitbox.hovered) {
            sb.draw(insertSlotGlowTexture, insertSlotX, insertSlotY, insertSlotWidth, insertSlotHeight);
        } else {
            sb.draw(insertSlotTexture, insertSlotX, insertSlotY, insertSlotWidth, insertSlotHeight);
        }

        insertHitbox.render(sb);
        insertSlotHitbox.render(sb);
    }
    private void openFileExplorer(String folderPath) {
        if (Desktop.isDesktopSupported()) {
            try {
                File file = new File(folderPath);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                } else {
                    System.err.println("Folder does not exist: " + folderPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to open folder: " + folderPath);
            }
        } else {
            System.err.println("Desktop is not supported on this platform.");
        }
    }

    //U&R CoinSlot and Purchasing Jukebox
    private Hitbox coinSlotHitbox = new Hitbox(63f * Settings.scale, 137f * Settings.scale);
    private void updateCoinSlot() {
        float coinSlotX = 1350f * Settings.scale;
        float coinSlotY = 620f * Settings.scale;
        float coinSlotWidth = 63f * Settings.scale;
        float coinSlotHeight = 137f * Settings.scale;

        coinSlotHitbox.move(coinSlotX + coinSlotWidth / 2f, coinSlotY + coinSlotHeight / 2f);
        coinSlotHitbox.update();

        if (coinSlotHitbox.hovered && InputHelper.justClickedLeft && !isCoinSlotClicked) {
            System.out.println("CoinSlot clicked!");
            if (AbstractDungeon.player.gold >= 5) {
                AbstractDungeon.player.loseGold(5);
                CardCrawlGame.sound.play("SHOP_PURCHASE", 0.1F); // Play the purchase sound
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                        (float) (Settings.WIDTH / 2),
                        (float) (Settings.HEIGHT / 2),
                        new JukeboxRelic()
                );
                isCoinSlotClicked = true; // Toggle clicked state

            }
        }
    }
    private void renderCoinSlot(SpriteBatch sb) {
        float coinSlotX = 1350f * Settings.scale;
        float coinSlotY = 620f * Settings.scale;
        float coinSlotWidth = 63f * Settings.scale;
        float coinSlotHeight = 137f * Settings.scale;
        float coinSlotThankyouX = 1325f * Settings.scale;
        float coinSlotThankyouY = 595f * Settings.scale;
        float coinSlotThankyouWidth = 150f * Settings.scale;
        float coinSlotThankyouHeight = 178f * Settings.scale;

        // Render CoinSlot with appropriate texture
        if (coinSlotHitbox.hovered && !isCoinSlotClicked) {
            sb.draw(coinSlotLitTexture, coinSlotX, coinSlotY, coinSlotWidth, coinSlotHeight);
        } else if (!coinSlotHitbox.hovered && !isCoinSlotClicked){
            sb.draw(coinSlotTexture, coinSlotX, coinSlotY, coinSlotWidth, coinSlotHeight);
        } else if (isCoinSlotClicked) {
            sb.draw(coinSlotThankyouTexture, coinSlotThankyouX, coinSlotThankyouY, coinSlotThankyouWidth, coinSlotThankyouHeight);
        }
        if (!isCoinSlotClicked) {
            FontHelper.renderFontLeftTopAligned(sb, FontHelper.buttonLabelFont, "5",
                    coinSlotX - 22f * Settings.scale,
                    coinSlotY + 25f * Settings.scale,
                    Color.GOLD); // Render text in gold color
        } // Render text in white color
        coinSlotHitbox.render(sb); // Render hitbox for debugging
    }




    //U&R Pagination Buttons Next and Previous
    private Hitbox nextButtonHitbox = new Hitbox(300f * Settings.scale, 85f * Settings.scale);
    private Hitbox prevButtonHitbox = new Hitbox(300f * Settings.scale, 85f * Settings.scale);

    private void updatePaginationButtons() {
        int totalPages = (int) Math.ceil((double) allTracks.size() / (BUTTONS_PER_PAGE * 2));

        float nextX = 1208f * Settings.scale;
        float nextY = 100f * Settings.scale;
        nextButtonHitbox.move(nextX, nextY + (85f * Settings.scale / 2f));
        nextButtonHitbox.update();

        if (currentPage < totalPages - 1 && nextButtonHitbox.hovered && InputHelper.justClickedLeft) {
            currentPage++;
            createButtons(); // Refresh buttons for the new page
            updateButtonHighlights(); // Ensure the highlight is updated for the new page
            System.out.println("Navigating to Next Page: " + currentPage);
        }

        float prevX = 508f * Settings.scale;
        float prevY = 100f * Settings.scale;
        prevButtonHitbox.move(prevX, prevY + (85f * Settings.scale / 2f));
        prevButtonHitbox.update();

        if (currentPage > 0 && prevButtonHitbox.hovered && InputHelper.justClickedLeft) {
            currentPage--;
            createButtons(); // Refresh buttons for the previous page
            updateButtonHighlights(); // Ensure the highlight is updated for the new page
            System.out.println("Navigating to Previous Page: " + currentPage);
        }
    }

    private void renderPaginationButtons(SpriteBatch sb) {
        int totalPages = (int) Math.ceil((double) allTracks.size() / (BUTTONS_PER_PAGE * 2));

        float nextX = 1208f * Settings.scale;
        float nextY = 100f * Settings.scale;
        float buttonWidth = 300f * Settings.scale;
        float buttonHeight = 85f * Settings.scale;

        if (currentPage < totalPages - 1) {
            sb.draw(buttonlongTexture, nextX - (buttonWidth / 2f), nextY, buttonWidth, buttonHeight);
            Color textColor = nextButtonHitbox.hovered ? Settings.GOLD_COLOR : Color.WHITE;
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, TEXT[26], nextX, nextY + (buttonHeight / 2), textColor); // "Next"
            nextButtonHitbox.render(sb); // Render the hitbox for debugging

        }

        float prevX = 508f * Settings.scale;
        float prevY = 100f * Settings.scale;

        // Render Previous button
        if (currentPage > 0) {
            sb.draw(buttonlongTexture, prevX - (buttonWidth / 2f), prevY, buttonWidth, buttonHeight);
            Color textColor = prevButtonHitbox.hovered ? Settings.GOLD_COLOR : Color.WHITE;
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, TEXT[27], prevX, prevY + (buttonHeight / 2), textColor);
            prevButtonHitbox.render(sb); // Render the hitbox for debugging
        }

        nextButtonHitbox.render(sb); // Render for debugging
        prevButtonHitbox.render(sb); // Render for debugging
    }

    private final Hitbox addToQueueHitbox = new Hitbox(62f * Settings.scale, 42f * Settings.scale);
    private void updateAddToQueueButton() {
        float buttonX = 1402f * Settings.scale; // Position above the Insert Slot
        float buttonY = 540f * Settings.scale;

        addToQueueHitbox.move(buttonX + addToQueueHitbox.width / 2f, buttonY + addToQueueHitbox.height / 2f);
        addToQueueHitbox.update();

        if (addToQueueHitbox.hovered && InputHelper.justClickedLeft) {
            if (!isQueueConfirmState) {
                // Change button to Confirm mode
                isQueueConfirmState = true;
                queuedTracks.clear();
                System.out.println("Queue selection mode activated.");
            } else {
                // Finalize the queue
                System.out.println("Queue confirmed: " + queuedTracks);
                isQueueConfirmState = false;
                playQueuedTracks();
            }
        }
    }
    private void renderAddToQueueButton(SpriteBatch sb) {
        float buttonX = 1402f * Settings.scale;
        float buttonY = 540f * Settings.scale;

        // Render button background
        sb.draw(isQueueConfirmState ? buttononTexture : buttonoffTexture, buttonX, buttonY, addToQueueHitbox.width, addToQueueHitbox.height);

        String buttonText = isQueueConfirmState ?  TEXT[30] : TEXT[28];
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, buttonText,
                buttonX + addToQueueHitbox.width / 2f, buttonY + addToQueueHitbox.height / 2f, Color.WHITE);


        addToQueueHitbox.render(sb);
    }
    private final Hitbox clearQueueHitbox = new Hitbox(50f * Settings.scale, 50f * Settings.scale);
    private void updateClearQueueButton() {
        float buttonX = 1409f * Settings.scale; // Position near Add to Queue button
        float buttonY = 468f * Settings.scale; // Slightly below Add to Queue button

        clearQueueHitbox.move(buttonX + clearQueueHitbox.width / 2f, buttonY + clearQueueHitbox.height / 2f);
        clearQueueHitbox.update();

        if (clearQueueHitbox.hovered && InputHelper.justClickedLeft) {
            queuedTracks.clear(); // Clear all tracks from the queue
            System.out.println( TEXT[30]);
        }
    }
    private void renderClearQueueButton(SpriteBatch sb) {
        float buttonX = 1409f * Settings.scale; // Position near Add to Queue button
        float buttonY = 468f * Settings.scale; // Slightly below Add to Queue button

        // Determine the button texture based on queue state
        Texture buttonTexture = queuedTracks.isEmpty() ? clearButtonOffTexture : clearButtonTexture;

        // Draw the button background
        sb.draw(buttonTexture, buttonX, buttonY, clearQueueHitbox.width, clearQueueHitbox.height);

        // Render button text
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont,  TEXT[29],
                buttonX + clearQueueHitbox.width / 2f, buttonY + clearQueueHitbox.height / 2f, Color.WHITE);

        // Render hitbox for debugging
        clearQueueHitbox.render(sb);
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

        if (isQueueConfirmState) {
            // Add/remove track from the queue
            if (queuedTracks.contains(selectedTrack)) {
                queuedTracks.remove(selectedTrack); // Unselect if already in queue
                System.out.println(TEXT[31] + " " + selectedTrack);
            } else {
                queuedTracks.add(selectedTrack); // Add to queue
                System.out.println(TEXT[32] + " "  + selectedTrack);
            }
        } else {
            textField = TEXT[23]   + selectedTrack;
            currentTrackName = formatTrackName(selectedTrack); // Update the current track name

            if (predefinedTracks.contains(selectedTrack)) {
                // Play predefined track
                String trackFileName = getTrackFileName(selectedTrack);
                System.out.println("Playing predefined track: " + trackFileName);
                stopCurrentMusic();
                CardCrawlGame.music.playTempBgmInstantly(trackFileName, loopEnabled); // Pass the loopEnabled flag
                isPlaying = true;
            } else {
                // Play custom track
                String originalTrackFileName = getOriginalFileName(selectedTrack);
                if (originalTrackFileName == null) {
                    System.err.println("File not found for track: " + selectedTrack);
                    return;
                }
                String trackPath = CUSTOM_MUSIC_FOLDER + File.separator + originalTrackFileName;
                playTempBgm(trackPath);
            }
        }
    }


    private String getTrackFileName(String key) {
        switch (key) {
            case "SHOP":
                return "STS_Merchant_NewMix_v1.ogg";
            case "SHRINE":
                return "STS_Shrine_NewMix_v1.ogg";
            case "MINDBLOOM":
                return "STS_Boss1MindBloom_v1.ogg";
            case "BOSS_BOTTOM":
                return "STS_Boss1_NewMix_v1.ogg";
            case "BOSS_CITY":
                return "STS_Boss2_NewMix_v1.ogg";
            case "BOSS_BEYOND":
                return "STS_Boss3_NewMix_v1.ogg";
            case "BOSS_ENDING":
                return "STS_Boss4_v6.ogg";
            case "ELITE":
                return "STS_EliteBoss_NewMix_v1.ogg";
            case "CREDITS":
                return "STS_Credits_v5.ogg";
            default:
                throw new IllegalArgumentException("Unknown track key: " + key);
        }
    }
    private String getOriginalFileName(String displayName) {
        File customFolder = new File(CUSTOM_MUSIC_FOLDER);
        if (!customFolder.exists()) return null;

        File[] files = customFolder.listFiles();
        if (files == null) return null;

        for (File file : files) {
            String trimmedName = file.getName()
                    .replaceAll("\\.(ogg|mp3|wav)$", "") // Remove extensions
                    .replace("_", " ");                 // Replace underscores with spaces
            if (trimmedName.equals(displayName)) {
                return file.getName(); // Return the exact file name with extension
            }
        }
        return null; // File not found
    }

    private void playTempBgm(String trackName) {
        stopCurrentMusic(); // Stop any current music
        try {
            // Construct the correct file path
            File file = new File(trackName);
            if (!file.exists()) {
                System.out.println("File not found: " + file.getAbsolutePath());
                return;
            }


            FileHandle fileHandle = Gdx.files.absolute(file.getAbsolutePath());
            nowPlayingSong = Gdx.audio.newMusic(fileHandle);
            currentTrackName = formatTrackName(file.getName());
            // Set looping based on the current loopEnabled state
            nowPlayingSong.setLooping(loopEnabled);
            nowPlayingSong.setVolume(Settings.MUSIC_VOLUME);
            nowPlayingSong.setOnCompletionListener(music -> {
                if (!loopEnabled) {
                    playRandomTrack(); // Use the improved playRandomTrack logic
                }
            });

            nowPlayingSong.play(); // Start playing the track
            System.out.println("Playing custom track: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to play music: " + trackName);
            e.printStackTrace();
        }
    }
    private void updateLoopingState() {
        if (nowPlayingSong != null) {
            nowPlayingSong.setLooping(loopEnabled); // Update looping based on the toggle
            System.out.println("Looping set to: " + loopEnabled);
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
    private void playRandomTrack() {
        if (allTracks.isEmpty()) {
            System.out.println("No tracks available to play.");
            return;
        }

        Random random = new Random();
        String nextTrack;

        // Loop until we find a track that's not the currently playing one
        do {
            int randomIndex = random.nextInt(allTracks.size());
            nextTrack = allTracks.get(randomIndex);
        } while (nextTrack.equals(currentTrackName)); // Use currentTrackName for comparison

        // Play the selected track
        textField = TEXT[23]   + nextTrack;
        currentTrackName = formatTrackName(nextTrack); // Update the current track name

        if (predefinedTracks.contains(nextTrack)) {
            String trackFileName = getTrackFileName(nextTrack);
            System.out.println("Playing predefined track: " + trackFileName);
            stopCurrentMusic();
            CardCrawlGame.music.playTempBgmInstantly(trackFileName, loopEnabled);
            isPlaying = true;
        } else {
            String originalFileName = getOriginalFileName(nextTrack);
            if (originalFileName != null) {
                String trackPath = CUSTOM_MUSIC_FOLDER + File.separator + originalFileName;
                playTempBgm(trackPath);
            } else {
                System.err.println("Failed to find file for track: " + nextTrack);
            }
        }

        // Update button highlights
        updateButtonHighlights();
    }

    private void clearHighlightsOnEmptyTrack() {
        if (textField == null || textField.equals(TEXT[22])) {
            for (int i = 0; i < buttonClickedStates.size(); i++) {
                buttonClickedStates.set(i, false);
            }
        }
    }

    private void playQueuedTracks() {
        if (queuedTracks.isEmpty()) {
            System.out.println(TEXT[25]); // "Queue is empty. Nothing to play."
            return;
        }

        // Play the first track in the queue
        String nextTrack = queuedTracks.remove(0);
        textField = TEXT[23] + nextTrack;
        currentTrackName = formatTrackName(nextTrack);

        if (predefinedTracks.contains(nextTrack)) {
            String trackFileName = getTrackFileName(nextTrack);
            System.out.println("Playing queued predefined track: " + trackFileName);
            stopCurrentMusic();
            CardCrawlGame.music.playTempBgmInstantly(trackFileName, loopEnabled); // Looping depends on toggle
            isPlaying = true;
        } else {
            String originalFileName = getOriginalFileName(nextTrack);
            if (originalFileName != null) {
                String trackPath = CUSTOM_MUSIC_FOLDER + File.separator + originalFileName;
                playTempBgm(trackPath);
            } else {
                System.err.println("Failed to find file for track: " + nextTrack);
            }
        }

        // Update the highlight for the currently playing track
        updateButtonHighlights();

        // Schedule the next track to play when the current one ends
        if (nowPlayingSong != null) {
            nowPlayingSong.setOnCompletionListener(music -> playQueuedTracks());
        }
    }
    private void updateButtonHighlights() {
        if (currentTrackName == null || currentTrackName.isEmpty()) {
            // Clear all highlights if there's no track playing
            for (int i = 0; i < buttonClickedStates.size(); i++) {
                buttonClickedStates.set(i, false);
            }
            return;
        }

        // Find the index of the currently playing track
        int trackIndex = allTracks.indexOf(currentTrackName);

        if (trackIndex == -1) {
            // Track not found in the current list, clear all highlights
            for (int i = 0; i < buttonClickedStates.size(); i++) {
                buttonClickedStates.set(i, false);
            }
            return;
        }

        // Determine if the track is on the current page
        int startIndex = currentPage * (BUTTONS_PER_PAGE * 2);
        int endIndex = Math.min(startIndex + (BUTTONS_PER_PAGE * 2), allTracks.size());

        if (trackIndex >= startIndex && trackIndex < endIndex) {
            // Calculate the button index relative to the current page
            int buttonIndex = trackIndex - startIndex;

            // Clear all highlights and set the current one
            for (int i = 0; i < buttonClickedStates.size(); i++) {
                buttonClickedStates.set(i, i == buttonIndex);
            }
        } else {
            // Track is not on the current page, clear all highlights
            for (int i = 0; i < buttonClickedStates.size(); i++) {
                buttonClickedStates.set(i, false);
            }
        }
    }

    public static class ScreenEnum {
        @SpireEnum
        public static AbstractDungeon.CurrentScreen JUKEBOX_SCREEN;
    }
}