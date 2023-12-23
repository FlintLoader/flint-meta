package net.flintloader.meta.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class InstallerVersion {
    String url;
    String maven;
    String version;
    @Setter boolean stable;
}
