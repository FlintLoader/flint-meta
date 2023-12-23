package net.flintloader.meta.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiVersion {

    String version;
    String minecraft;
    String maven;
    String url;

}
