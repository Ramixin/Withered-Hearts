package net.ramixin.witherhearts.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(Gui.class)
public class GuiMixin {

    @WrapOperation(method = "extractHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui$HeartType;forPlayer(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/client/gui/Gui$HeartType;"))
    private Gui.HeartType preventWitherHeartRenderMode(Player player, Operation<Gui.HeartType> original, @Share("witheredHeartCount") LocalIntRef witheredHeartCount) {
        Gui.HeartType type = original.call(player);
        MobEffectInstance effect = player.getEffect(MobEffects.WITHER);
        if(effect == null) witheredHeartCount.set(0);
        else {
            int duration = effect.isInfiniteDuration() ? Integer.MAX_VALUE : effect.getDuration();
            int i = Math.max(40 >> effect.getAmplifier(), 10);
            witheredHeartCount.set(duration / i);
        }
        if(type == Gui.HeartType.WITHERED) return Gui.HeartType.NORMAL;
        return type;
    }

    @WrapOperation(method = "extractHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractHeart(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Gui$HeartType;IIZZZ)V"))
    private void preventWitherHeartIfRemaining(Gui instance, GuiGraphicsExtractor guiGraphics, Gui.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half, Operation<Void> original, @Share("witheredHeartCount") LocalIntRef witheredHeartCount) {
        Gui.HeartType changedType;
        int left = witheredHeartCount.get();
        boolean renderHalf = false;
        if(left <= 0) changedType = type;
        else if(type == Gui.HeartType.CONTAINER) changedType = type;
        else if (half) {
            changedType = Gui.HeartType.WITHERED;
            witheredHeartCount.set(left - 1);
        }
        else if(left > 1) {
            changedType = Gui.HeartType.WITHERED;
            witheredHeartCount.set(left - 2);
        }
        else {
            changedType = type;
            renderHalf = true;
            witheredHeartCount.set(0);
        }
        original.call(instance, guiGraphics, changedType, x, y, hardcore, blinking, half);
        if(renderHalf) guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, generatedId(Gui.HeartType.WITHERED.getSprite(hardcore, true, blinking).getPath()), x, y, 9, 9);
    }

    @Unique
    private static Identifier generatedId(String path) {
        return Identifier.fromNamespaceAndPath("witherhearts", path.replace("/withered", "/flipped_withered"));
    }

}
