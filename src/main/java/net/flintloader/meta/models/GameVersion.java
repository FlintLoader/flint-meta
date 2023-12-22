/**
 * This file is part of flint-meta and is licensed under the MIT License
 */
package net.flintloader.meta.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author HypherionSA
 * Minecraft Version DTO
 */
@AllArgsConstructor
@Getter
public class GameVersion {

    String version;
    @Setter
    boolean stable = false;

}
