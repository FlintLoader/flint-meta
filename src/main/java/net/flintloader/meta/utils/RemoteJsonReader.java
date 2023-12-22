/**
 * This file is part of flint-meta and is licensed under the MIT License
 */
package net.flintloader.meta.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author HypherionSA
 * Utility class to read a JSON file from a remote URL
 */
public class RemoteJsonReader {

    public static JsonObject readJsonFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            return JsonParser.parseString(jsonContent.toString()).getAsJsonObject();
        }
    }

}
