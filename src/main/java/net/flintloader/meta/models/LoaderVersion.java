/**
 * This file is part of flint-meta and is licensed under the MIT License
 */
package net.flintloader.meta.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author HypherionSA
 * Loader Version DTO
 */
@AllArgsConstructor
@Getter
public class LoaderVersion {

    String version;
    String maven;
    String url;

}
