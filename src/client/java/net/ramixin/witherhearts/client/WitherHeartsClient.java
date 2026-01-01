package net.ramixin.witherhearts.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.Identifier;

public class WitherHeartsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

    }

    public static Identifier generatedId(String path) {
        return Identifier.fromNamespaceAndPath("witherhearts", path.replace("/withered", "/flipped_withered"));
    }
}
