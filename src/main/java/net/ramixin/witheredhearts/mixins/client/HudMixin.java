package net.ramixin.witheredhearts.mixins.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
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
@Mixin(Hud.class)
public class HudMixin {

    @WrapOperation(method = "extractHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud$HeartType;forPlayer(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/client/gui/Hud$HeartType;"))
    private Hud.HeartType preventWitherHeartRenderMode(Player player, Operation<Hud.HeartType> original, @Share("witheredHeartCount") LocalIntRef witheredHeartCount) {
        Hud.HeartType type = original.call(player);
        MobEffectInstance effect = player.getEffect(MobEffects.WITHER);
        if(effect == null) witheredHeartCount.set(0);
        else {
            int duration = effect.isInfiniteDuration() ? Integer.MAX_VALUE : effect.getDuration();
            int i = Math.max(40 >> effect.getAmplifier(), 10);
            witheredHeartCount.set(duration / i);
        }
        if(type == Hud.HeartType.WITHERED) return Hud.HeartType.NORMAL;
        return type;
    }

    @WrapOperation(method = "extractHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractHeart(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Hud$HeartType;IIZZZ)V"))
    private void preventWitherHeartIfRemaining(Hud instance, GuiGraphicsExtractor graphics, Hud.HeartType type, int xo, int yo, boolean isHardcore, boolean blinks, boolean half, Operation<Void> original, @Share("witheredHeartCount") LocalIntRef witheredHeartCount) {
        Hud.HeartType changedType;
        int left = witheredHeartCount.get();
        boolean renderHalf = false;
        if(left <= 0) changedType = type;
        else if(type == Hud.HeartType.CONTAINER) changedType = type;
        else if (half) {
            changedType = Hud.HeartType.WITHERED;
            witheredHeartCount.set(left - 1);
        }
        else if(left > 1) {
            changedType = Hud.HeartType.WITHERED;
            witheredHeartCount.set(left - 2);
        }
        else {
            changedType = type;
            renderHalf = true;
            witheredHeartCount.set(0);
        }
        original.call(instance, graphics, changedType, xo, yo, isHardcore, blinks, half);
        if(renderHalf) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, generatedId(Hud.HeartType.WITHERED.getSprite(isHardcore, true, blinks).getPath()), xo, yo, 9, 9);
    }

    @Unique
    private static Identifier generatedId(String path) {
        return Identifier.fromNamespaceAndPath("witherhearts", path.replace("/withered", "/flipped_withered"));
    }

}
