package net.ramixin.witherhearts.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.ramixin.witherhearts.client.WitherHeartsClient;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @WrapOperation(method = "renderHealthBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud$HeartType;fromPlayerState(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/client/gui/hud/InGameHud$HeartType;"))
    private InGameHud.HeartType preventWitherHeartRenderMode(PlayerEntity player, Operation<InGameHud.HeartType> original, @Share("witheredHeartCount") LocalIntRef witheredHeartCount) {
        InGameHud.HeartType type = original.call(player);
        StatusEffectInstance effect = player.getStatusEffect(StatusEffects.WITHER);
        if(effect == null) witheredHeartCount.set(0);
        else witheredHeartCount.set(effect.getDuration() / (40 >> effect.getAmplifier()));
        if(type == InGameHud.HeartType.WITHERED) return InGameHud.HeartType.NORMAL;
        return type;
    }

    @WrapOperation(method = "renderHealthBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawHeart(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/gui/hud/InGameHud$HeartType;IIZZZ)V"))
    private void preventWitherHeartIfRemaining(InGameHud instance, DrawContext context, InGameHud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half, Operation<Void> original, @Share("witheredHeartCount") LocalIntRef witheredHeartCount) {
        InGameHud.HeartType changedType;
        int left = witheredHeartCount.get();
        boolean renderHalf = false;
        if(left <= 0) changedType = type;
        else if(type == InGameHud.HeartType.CONTAINER) changedType = type;
        else if (half) {
            changedType = InGameHud.HeartType.WITHERED;
            witheredHeartCount.set(left - 1);
        }
        else if(left > 1) {
            changedType = InGameHud.HeartType.WITHERED;
            witheredHeartCount.set(left - 2);
        }
        else {
            changedType = type;
            renderHalf = true;
            witheredHeartCount.set(0);
        }
        original.call(instance, context, changedType, x, y, hardcore, blinking, half);
        if(renderHalf) context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, WitherHeartsClient.generatedId(InGameHud.HeartType.WITHERED.getTexture(hardcore, true, blinking).getPath()), x, y, 9, 9);
    }

}
