package spireCafe.interactables.patrons.dandaleftnut;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.cards.AbstractCard;

import basemod.helpers.CardModifierManager;
import spireCafe.util.Wiz;

@SpirePatch2(clz = AbstractCard.class, method = "renderImage")
public class GhostModifierPatch {

    public static void Prefix(AbstractCard __instance, SpriteBatch sb) {
        if (Wiz.p() != null && CardModifierManager.hasModifier(__instance, GhostModifier.ID)) {
            ((GhostModifier) CardModifierManager.getModifiers(__instance, GhostModifier.ID).get(0)).render(__instance,
                    sb);
        }

    }

}
