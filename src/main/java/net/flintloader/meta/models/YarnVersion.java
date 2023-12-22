/**
 * This file is part of flint-meta and is licensed under the MIT License
 */
package net.flintloader.meta.models;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.flintloader.meta.maven.MavenRepository;

/**
 * @author HypherionSA
 * Yarn Mapping Version DTO
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class YarnVersion {
    String gameVersion;
    String maven;
    String version;
    String separator;
    int build;
    boolean stable;

    public static YarnVersion of(MavenRepository.ArtifactMetadata.Artifact artifact) {
        String[] v;

        if (artifact.getVersion().contains("+build.")) {
            v = artifact.getVersion().split("\\+build.");
        } else {
            v = new String[]{artifact.getVersion(), "0"};
        }

        return new YarnVersion(v[0], artifact.mavenId(), artifact.getVersion(), "+build.", Integer.parseInt(v[1]), false);
    }
}
