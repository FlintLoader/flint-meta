/**
 * This file is part of flint-meta and is licensed under the MIT License
 */
package net.flintloader.meta.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author HypherionSA
 * /versions endpoint DTO
 */
@AllArgsConstructor
@Getter
public class AllVersions {

    List<GameVersion> game;
    List<LoaderVersion> loaders;
    List<YarnVersion> mappings;
    List<IntermediaryVersion> intermediary;
    List<InstallerVersion> installers;

}
