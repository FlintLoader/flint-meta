/**
 * This file is copied from https://github.com/QuiltMC/update-quilt-meta/blob/main/src/main/java/org/quiltmc/MinecraftMeta.java
 * and is licensed under the MIT license.
 * See License here: https://github.com/QuiltMC/update-quilt-meta/blob/main/LICENSE
 */
package net.flintloader.meta.maven;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MinecraftMaven {

    static final URL MANIFEST;

    static {
        URL url = null;

        try {
            url = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load Minecraft version manifest");
        }

        MANIFEST = url;
    }

    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    private List<Version> versions;

    private MinecraftMaven() {
    }

    public static JsonArray get(MavenRepository.ArtifactMetadata hashedMojmap, Gson gson) {
        JsonArray versions = new JsonArray();

        InputStreamReader reader;
        try {
            reader = new InputStreamReader(MANIFEST.openStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load Minecraft version manifest");
        }
        MinecraftMaven meta = gson.fromJson(reader, MinecraftMaven.class);

        for (Version version : meta.versions) {
            if (hashedMojmap.contains(version.id)) {
                JsonObject object = new JsonObject();

                object.addProperty("version", version.id);
                object.addProperty("stable", version.type.equals("release"));

                versions.add(object);
            }
        }

        return versions;
    }

    public static List<Version> getAll(Gson gson) {

        InputStreamReader reader;
        try {
            reader = new InputStreamReader(MANIFEST.openStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load Minecraft version manifest");
        }
        MinecraftMaven meta = gson.fromJson(reader, MinecraftMaven.class);

        return new ArrayList<>(meta.versions);
    }

    @Getter
    public static class Version {
        String id;
        String type;
    }

}
