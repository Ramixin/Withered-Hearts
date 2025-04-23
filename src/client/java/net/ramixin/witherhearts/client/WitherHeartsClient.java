package net.ramixin.witherhearts.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.ramixin.mixson.debug.DebugMode;
import net.ramixin.mixson.inline.Mixson;
import net.ramixin.mixson.inline.MixsonCodec;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WitherHeartsClient implements ClientModInitializer {

    public static final String MOD_ID = "witherhearts";

    public static final MixsonCodec<BufferedImage> BUFFERED_IMAGE_PNG_MIXSON_CODEC = MixsonCodec.create("png",
            resource -> ImageIO.read(resource.getInputStream()),
            (r, elem) -> new Resource(r.getPack(), () -> new ByteArrayInputStream(bufferedImageToStream(elem).toByteArray()), r::getMetadata),
            WitherHeartsClient::bufferedImageToStream
    );

    @Override
    public void onInitializeClient() {

        Mixson.setDebugMode(DebugMode.EXPORT);
        Mixson.registerEvent(
                BUFFERED_IMAGE_PNG_MIXSON_CODEC,
                Mixson.DEFAULT_PRIORITY,
                id -> id.getNamespace().equals("minecraft") && id.getPath().startsWith("textures/gui/sprites/hud/heart/withered") && id.getPath().contains("half"),
                "generateFlippedHalfHeartTextures",
                context -> {
                    BufferedImage normal = context.getFile();
                    BufferedImage flipped = new BufferedImage(normal.getWidth(), normal.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    for(int x = 0; x < normal.getWidth(); x++) {
                        for(int y = 0; y < normal.getHeight(); y++) {
                            flipped.setRGB(x, y, normal.getRGB(normal.getWidth() - x - 1, y));
                        }
                    }
                    Identifier id = generatedId(context.getResourceId().getPath());
                    context.createResource(id, flipped);
                },
                false
        );

    }

    public static Identifier generatedId(String path) {
        return Identifier.of(MOD_ID, path.replace("/withered", "/generated/flipped_withered"));
    }

    static ByteArrayOutputStream bufferedImageToStream(BufferedImage image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stream;
    }
}
