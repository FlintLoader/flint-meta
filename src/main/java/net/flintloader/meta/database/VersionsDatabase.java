/**
 * This file contains content from https://github.com/FabricMC/fabric-meta
 * It is licensed under Apache 2.0.
 * See License here: https://github.com/FabricMC/fabric-meta/blob/master/LICENSE
 */
package net.flintloader.meta.database;

import com.google.gson.Gson;
import lombok.Getter;
import net.flintloader.meta.Constants;
import net.flintloader.meta.maven.MavenRepository;
import net.flintloader.meta.maven.MinecraftMaven;
import net.flintloader.meta.models.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.flintloader.meta.FlintMeta.LOGGER;

/**
 * @author HypherionSA
 * In Memory Database to store version information, preventing unnecessary requests
 */
public class VersionsDatabase {

    // Main Flint Maven Repository
    private static final MavenRepository flintMaven = new MavenRepository(Constants.FLINT_MAVEN);
    // Flint Mirror repository for mirrored fabric libraries
    private static final MavenRepository flintMirror = new MavenRepository(Constants.FLINT_MIRROR);
    private static final Gson GSON = new Gson();

    @Getter
    private List<GameVersion> game;
    @Getter
    private List<LoaderVersion> loaders;
    @Getter
    private List<YarnVersion> mappings;
    @Getter
    private List<IntermediaryVersion> intermediary;
    @Getter
    private List<InstallerVersion> installers;

    /**
     * Get the index of a minecraft version from the launcher manifest
     * @param versions List of Minecraft Versions
     * @param version The version to check for
     * @return The index of the version, or 0 if it's not found
     */
    private static int getIndex(List<MinecraftMaven.Version> versions, String version) {
        for (int i = 0; i < versions.size(); i++) {
            if (versions.get(i).getId().equals(version)) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Extract a specific minecraft version from a list of versions
     * @param versions List of Minecraft Versions
     * @param version The version to check for
     * @return The version or null if not found
     */
    @Nullable
    private static MinecraftMaven.Version getMinecraftVersion(List<MinecraftMaven.Version> versions, String version) {
        if (versions.stream().anyMatch(v -> v.getId().equals(version))) {
            return versions.stream().filter(v -> v.getId().equals(version)).findFirst().get();
        }
        return null;
    }

    /**
     * Build the database cache. Called every 2 minutes
     * @throws IOException Thrown when an error occurs
     */
    public void generateDatabase() throws IOException {
        long start = System.currentTimeMillis();
        loaders = loadLoaders();
        mappings = loadMappings();
        intermediary = loadIntermediary();
        game = loadGameVersions();
        installers = loadInstallers();

        LOGGER.info("DB update took {} ms", (System.currentTimeMillis() - start));
    }

    /**
     * Load supported minecraft versions based on the available intermediary fabric mappings
     * @return A list of supported game versions
     */
    private List<GameVersion> loadGameVersions() {
        List<GameVersion> versions = new ArrayList<>();
        List<MinecraftMaven.Version> minecraftVersions = MinecraftMaven.getAll(GSON);

        // This section of code comes from https://github.com/FabricMC/fabric-meta
        // and is licensed under Apache-2.0
        intermediary = new ArrayList<>(intermediary);
        intermediary.sort(Comparator.comparingInt(o -> getIndex(minecraftVersions, o.getVersion())));
        intermediary.forEach(version -> version.setStable(true));

        intermediary.removeIf(o -> minecraftVersions.stream().noneMatch(version -> version.getId().equals(o.getVersion())));

        if (intermediary.isEmpty()) {
            minecraftVersions.forEach(v -> versions.add(new GameVersion(v.getId(), v.getType().equalsIgnoreCase("release"))));
        } else {
            intermediary.forEach(i -> {
                if (versions.stream().noneMatch(vv -> vv.getVersion().equals(i.getVersion()))) {
                    MinecraftMaven.Version version = getMinecraftVersion(minecraftVersions, i.getVersion());
                    if (version == null) return;

                    versions.add(new GameVersion(version.getId(), version.getType().equals("release")));
                }
            });
        }
        // End of Apache-2.0 code

        return versions;
    }

    /**
     * Get a list of available Flint Loader version from the maven
     * @return A list of loaders
     * @throws IOException Thrown when an error occurs
     */
    private List<LoaderVersion> loadLoaders() throws IOException {
        List<LoaderVersion> versions = new ArrayList<>();
        MavenRepository.ArtifactMetadata metadata = flintMaven.getMetadata(Constants.FLINT_GROUP, Constants.LOADER_ARTIFACT);

        for (MavenRepository.ArtifactMetadata.Artifact artifact : metadata) {
            versions.add(new LoaderVersion(artifact.getVersion(), artifact.mavenId(), artifact.url()));
        }

        return versions;
    }

    /**
     * Get a list of available Yarn Mappings from the Mirror maven
     * @return A list of Yarn Mappings
     * @throws IOException Thrown when an error occurs
     */
    private List<YarnVersion> loadMappings() throws IOException {
        List<YarnVersion> versions = new ArrayList<>();
        MavenRepository.ArtifactMetadata metadata = flintMirror.getMetadata(Constants.FABRIC_GROUP, Constants.YARN_ARTIFACT);

        for (MavenRepository.ArtifactMetadata.Artifact artifact : metadata) {
            versions.add(YarnVersion.of(artifact));
        }

        return versions;
    }

    /**
     * Get a list of intermediary mappings from the Mirror Maven
     * @return A list of Intermediary mappings
     * @throws IOException Thrown when an error occurs
     */
    private List<IntermediaryVersion> loadIntermediary() throws IOException {
        List<IntermediaryVersion> versions = new ArrayList<>();
        MavenRepository.ArtifactMetadata metadata = flintMirror.getMetadata(Constants.FABRIC_GROUP, Constants.INTERMEDIARY_ARTIFACT);

        for (MavenRepository.ArtifactMetadata.Artifact artifact : metadata) {
            versions.add(new IntermediaryVersion(artifact.mavenId(), artifact.getVersion(), true));
        }

        return versions;
    }

    /**
     * Get a list of installer versions from the flint Maven
     * @return A list of installer versions
     * @throws IOException Thrown when an error occurs
     */
    private List<InstallerVersion> loadInstallers() throws IOException {
        List<InstallerVersion> versions = new ArrayList<>();
        MavenRepository.ArtifactMetadata metadata = flintMaven.getMetadata(Constants.FLINT_GROUP, Constants.INSTALLER_ARTIFACT);

        for (MavenRepository.ArtifactMetadata.Artifact artifact : metadata) {
            versions.add(new InstallerVersion(artifact.url(), artifact.mavenId(), artifact.getVersion(), true));
        }

        return versions;
    }
}
