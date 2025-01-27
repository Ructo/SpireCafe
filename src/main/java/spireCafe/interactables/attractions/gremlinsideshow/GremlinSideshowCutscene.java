package spireCafe.interactables.attractions.gremlinsideshow;

import basemod.BaseMod;
import spireCafe.Anniv7Mod;
import spireCafe.abstracts.AbstractCutscene;
import spireCafe.abstracts.AbstractNPC;
import spireCafe.screens.GremlinSideshowScreen;
import spireCafe.util.cutsceneStrings.CutsceneStrings;
import spireCafe.util.cutsceneStrings.LocalizedCutsceneStrings;

public class GremlinSideshowCutscene extends AbstractCutscene {
    public static final String ID = Anniv7Mod.makeID(GremlinSideshowCutscene.class.getSimpleName());
    private static final CutsceneStrings cutsceneStrings = LocalizedCutsceneStrings.getCutsceneStrings(ID);

    public GremlinSideshowCutscene(AbstractNPC character) {
        super(character, cutsceneStrings);
    }

    @Override
    protected void onClick() {
        if (dialogueIndex == 0) {
            this.dialog.addDialogOption("Look at the buttons").setOptionResult((i) -> {
                BaseMod.openCustomScreen(GremlinSideshowScreen.ScreenEnum.GREMLIN_SIDESHOW_SCREEN); // Open the custom screen
            });

            this.dialog.addDialogOption("Back").setOptionResult((i) -> {
                goToDialogue(2); // End interaction
            });
        } else {
            endCutscene(); // End the cutscene
        }
    }
}