package spireCafe.interactables.attractions.gremlinsideshow;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import spireCafe.Anniv7Mod;
import spireCafe.abstracts.AbstractAttraction;
import spireCafe.util.TexLoader;

public class GremlinSideshowAttraction extends AbstractAttraction {
    public static final String ID = GremlinSideshowAttraction.class.getSimpleName();
    private static final CharacterStrings characterStrings = CardCrawlGame.languagePack.getCharacterString(Anniv7Mod.makeID(ID));


    public GremlinSideshowAttraction(float animationX, float animationY) {
        super(animationX, animationY, 350, 470);
        authors = "Ninja Puppy";
        name = characterStrings.NAMES[0];
        img = TexLoader.getTexture(Anniv7Mod.makeAttractionPath("bookshelf/bookshelf.png"));
    }

    @Override
    public void renderCutscenePortrait(SpriteBatch sb) {

    }

    @Override
    public void renderAnimation(SpriteBatch sb) {
        super.renderAnimation(sb);
    }


    @Override
    public void onInteract() {

        if (!alreadyPerformedTransaction) {
            AbstractDungeon.topLevelEffectsQueue.add(new GremlinSideshowCutscene(this));
        }
    }
}
