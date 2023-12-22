/**
 * This file is part of flint-meta and is licensed under the MIT License
 */
package net.flintloader.meta.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author HypherionSA
 * Intermediary Version DTO
 */
@Getter
@AllArgsConstructor
public class IntermediaryVersion {
    String maven;
    String version;
    @Setter
    boolean stable;
}
