/**
 * This file contains content from https://github.com/FabricMC/fabric-meta
 * It is licensed under Apache 2.0.
 * See License here: https://github.com/FabricMC/fabric-meta/blob/master/LICENSE
 */
package net.flintloader.meta.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.flintloader.meta.Constants;
import net.flintloader.meta.models.LoaderInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author HypherionSA
 * Minecraft Launcher profile builder
 * Copied from https://github.com/FabricMC/fabric-meta/blob/70fdf661eb42a4afa9b602474b7514e90ce9a9ae/src/main/java/net/fabricmc/meta/web/ProfileHandler.java#L107
 */
public class ProfileUtils {

    private static final DateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public static JsonObject buildProfileJson(LoaderInfo info, String side) {
        JsonObject launcherMeta = info.getLauncherMeta();

        String profileName = String.format("punch-%s-%s", info.getLoader().getVersion(), info.getIntermediary().getVersion());

        JsonObject librariesObject = launcherMeta.get("libraries").getAsJsonObject();
        // Build the libraries array with the existing libs + loader and intermediary
        JsonArray libraries = (JsonArray) librariesObject.get("common");
        libraries.add(formatLibrary(info.getIntermediary().getMaven(), Constants.FLINT_MIRROR));
        libraries.add(formatLibrary(info.getLoader().getMaven(), Constants.FLINT_MAVEN));

        if (librariesObject.has(side)) {
            libraries.addAll(librariesObject.get(side).getAsJsonArray());
        }

        String currentTime = ISO_8601.format(new Date());

        JsonObject profile = new JsonObject();
        profile.addProperty("id", profileName);
        profile.addProperty("inheritsFrom", info.getIntermediary().getVersion());
        profile.addProperty("releaseTime", currentTime);
        profile.addProperty("time", currentTime);
        profile.addProperty("type", "release");

        JsonElement mainClassElement = launcherMeta.get("mainClass");
        String mainClass;

        if (mainClassElement.isJsonObject()) {
            mainClass = mainClassElement.getAsJsonObject().get(side).getAsString();
        } else {
            mainClass = mainClassElement.getAsString();
        }

        profile.addProperty("mainClass", mainClass);

        JsonObject arguments = new JsonObject();

        // I believe this is required to stop the launcher from complaining
        arguments.add("game", new JsonArray());

        if (side.equals("client")) {
            // add '-DFabricMcEmu= net.minecraft.client.main.Main ' to emulate vanilla MC presence for programs that check the process command line (discord, nvidia hybrid gpu, ..)
            JsonArray jvmArgs = new JsonArray();
            jvmArgs.add("-DFlintMcEmu= net.minecraft.client.main.Main ");
            arguments.add("jvm", jvmArgs);
        }

        profile.add("arguments", arguments);

        profile.add("libraries", libraries);

        return profile;
    }

    private static JsonObject formatLibrary(String mavenPath, String url) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", mavenPath);
        jsonObject.addProperty("url", url);
        return jsonObject;
    }

}
