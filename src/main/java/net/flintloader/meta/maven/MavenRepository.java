/**
 * This file is copied from https://github.com/QuiltMC/update-quilt-meta/blob/main/src/main/java/org/quiltmc/MavenRepository.java
 * and is licensed under the MIT license.
 * See License here: https://github.com/QuiltMC/update-quilt-meta/blob/main/LICENSE
 */
package net.flintloader.meta.maven;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@RequiredArgsConstructor
public class MavenRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenRepository.class);

    final String url;

    private static Collection<String> readVersionsFromPom(String path) throws IOException {
        Collection<String> versions = new LinkedHashSet<>();

        try {
            URL url = new URL(path);
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(url.openStream());

            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("version")) {
                    String text = reader.getElementText();
                    versions.add(text);
                }
            }

            reader.close();
            List<String> list = new ArrayList<>(versions);
            Collections.reverse(list);
            versions.clear();
            versions.addAll(list);

        } catch (IOException | XMLStreamException e) {
            LOGGER.error("Failed to load " + path, e);
        }

        return versions;
    }

    public ArtifactMetadata getMetadata(String group, String name) throws IOException {
        Collection<String> versions = readVersionsFromPom(String.format("%s%s/%s/maven-metadata.xml",
                this.url,
                String.join("/", group.split("\\.")),
                name
        ));

        return new ArtifactMetadata(group, name, versions);
    }

    public class ArtifactMetadata implements Iterable<ArtifactMetadata.Artifact> {

        final String group;
        final String name;
        final Collection<String> versions;
        final Collection<Artifact> artifacts;

        ArtifactMetadata(String group, String name, Collection<String> versions) {
            this.group = group;
            this.name = name;
            this.versions = versions;
            this.artifacts = new LinkedHashSet<>();

            for (String version : versions) {
                this.artifacts.add(new Artifact(version));
            }
        }

        @NotNull
        @Override
        public Iterator<ArtifactMetadata.Artifact> iterator() {
            return this.artifacts.iterator();
        }

        public boolean contains(String version) {
            return this.versions.contains(version);
        }

        @AllArgsConstructor
        public class Artifact {

            @Getter
            final String version;

            public String mavenId() {
                return String.format("%s:%s:%s", ArtifactMetadata.this.group, ArtifactMetadata.this.name, this.version);
            }

            public String group() {
                return ArtifactMetadata.this.group;
            }

            public String name() {
                return ArtifactMetadata.this.name;
            }

            public String url() {
                return url("jar");
            }

            public String url(String ext) {
                return String.format("%s%s/%s/%s/%s-%s." + ext,
                        MavenRepository.this.url,
                        ArtifactMetadata.this.group.replaceAll("\\.", "/"),
                        ArtifactMetadata.this.name,
                        this.version,
                        ArtifactMetadata.this.name,
                        this.version
                );
            }
        }
    }

}
