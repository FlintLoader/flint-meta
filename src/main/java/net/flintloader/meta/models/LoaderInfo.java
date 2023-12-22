/**
 * This file is part of flint-meta and is licensed under the MIT License
 */
package net.flintloader.meta.models;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author HypherionSA
 * Loader Info DTO
 */
@AllArgsConstructor
@Getter
public class LoaderInfo {
    LoaderVersion loader;
    IntermediaryVersion intermediary;
    JsonObject launcherMeta;
}
