/**
 * This file is part of flint-meta and is licensed under the MIT License
 */
package net.flintloader.meta.web.routes.v1;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import net.flintloader.meta.Constants;
import net.flintloader.meta.models.*;
import net.flintloader.meta.utils.ProfileUtils;
import net.flintloader.meta.utils.RemoteJsonReader;
import net.flintloader.meta.web.WebServer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static net.flintloader.meta.FlintMeta.versionsDatabase;

/**
 * @author HypherionSA
 * Main V1 Route Controller
 */
public class VersionsRoute {
    private final String BASE_PATH = "/v1/versions";

    public VersionsRoute() {
        register();
    }

    private void register() {
        WebServer.javalin.routes(() -> path(BASE_PATH, () -> {
            get("/", this::getAllVersions);
            get("/game", this::getGameVersions);
            get("/yarn", this::getYarnVersions);
            get("/yarn/{gameVersion}", this::getFilteredYarnVersions);
            get("/intermediary", this::getIntermediary);
            get("/intermediary/{gameVersion}", this::getIntermediaryFiltered);
            get("/loader", this::getLoaders);
            get("/loader/{gameVersion}", this::getLoadersFiltered);
            get("/loader/{gameVersion}/{loaderVersion}", this::getSingleLoader);
            get("/loader/{gameVersion}/{loaderVersion}/profile/json", this::getLoaderJsonProfile);
            get("/installer", this::getInstallers);
        }));
    }

    @OpenApi(
            path = "/versions/installer",
            methods = { HttpMethod.GET },
            description = "Lists all Flint Installer versions",
            versions = "v1",
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JSON arrays of Flint Installer versions",
                            content = { @OpenApiContent(from = InstallerVersion[].class )}
                    )
            }
    )
    private void getInstallers(@NotNull Context context) {
        WebServer.jsonResponse(context, versionsDatabase.getInstallers());
    }

    @OpenApi(
            path = "/versions/loader/game/version/profile/json",
            methods = { HttpMethod.GET },
            description = "Get a specific minecraft launcher loader profile for a game version",
            versions = "v1",
            pathParams = {
                    @OpenApiParam(
                            name = "game",
                            description = "The game version you want to list Flint Loader versions for",
                            required = true
                    ),
                    @OpenApiParam(
                            name = "version",
                            description = "The Flint Loader version to check for",
                            required = true
                    )
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JSON arrays of Flint Loader versions",
                            content = { @OpenApiContent(from = JsonObject[].class )}
                    )
            }
    )
    private void getLoaderJsonProfile(@NotNull Context context) {
        String gameVersion = context.pathParam("gameVersion");
        String loaderVersion = context.pathParam("loaderVersion");

        LoaderVersion loader = versionsDatabase.getLoaders().stream().filter(v -> v.getVersion().equals(loaderVersion)).findFirst().orElse(null);
        IntermediaryVersion mapping = versionsDatabase.getIntermediary().stream().filter(v -> v.getVersion().equalsIgnoreCase(gameVersion)).findFirst().orElse(null);

        if (loader == null) {
            context.result("No loader version found for " + gameVersion).status(400);
            return;
        }

        if (mapping == null) {
            context.result("No mappings found for " + gameVersion).status(400);
            return;
        }

        LoaderInfo loaderInfo = buildLoaderInfo(loader, mapping);
        WebServer.jsonResponse(context, ProfileUtils.buildProfileJson(loaderInfo, "client"));
    }

    @OpenApi(
            path = "/versions/loader/game/version",
            methods = { HttpMethod.GET },
            description = "Get a specific loader profile for a game version",
            versions = "v1",
            pathParams = {
                    @OpenApiParam(
                            name = "game",
                            description = "The game version you want to list Flint Loader versions for",
                            required = true
                    ),
                    @OpenApiParam(
                            name = "version",
                            description = "The Flint Loader version to check for",
                            required = true
                    )
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JSON arrays of Flint Loader versions",
                            content = { @OpenApiContent(from = LoaderInfo[].class )}
                    )
            }
    )
    private void getSingleLoader(@NotNull Context context) {
        String gameVersion = context.pathParam("gameVersion");
        String loaderVersion = context.pathParam("loaderVersion");

        LoaderVersion loader = versionsDatabase.getLoaders().stream().filter(v -> v.getVersion().equals(loaderVersion)).findFirst().orElse(null);
        IntermediaryVersion mapping = versionsDatabase.getIntermediary().stream().filter(v -> v.getVersion().equalsIgnoreCase(gameVersion)).findFirst().orElse(null);

        if (loader == null) {
            context.result("No loader version found for " + gameVersion).status(400);
            return;
        }

        if (mapping == null) {
            context.result("No mappings found for " + gameVersion).status(400);
            return;
        }

        WebServer.jsonResponse(context, buildLoaderInfo(loader, mapping));
    }

    @OpenApi(
            path = "/versions/loader/game",
            methods = { HttpMethod.GET },
            description = "Lists all Flint Loader versions and intermediary mappings for a game version",
            versions = "v1",
            pathParams = {
                    @OpenApiParam(
                            name = "game",
                            description = "The game version you want to list Flint Loader versions for",
                            required = true
                    )
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JSON arrays of Flint Loader versions",
                            content = { @OpenApiContent(from = LoaderInfo[].class )}
                    )
            }
    )
    private void getLoadersFiltered(@NotNull Context context) {
        String gameVersion = context.pathParam("gameVersion");

        IntermediaryVersion mapping = versionsDatabase.getIntermediary().stream().filter(o -> o.getVersion().equals(gameVersion)).findFirst().orElse(null);

        if (mapping == null) {
            WebServer.jsonResponse(context, new ArrayList<>());
            return;
        }

        List<LoaderInfo> infos = new ArrayList<>();

        for (LoaderVersion loader : versionsDatabase.getLoaders()) {
            infos.add(buildLoaderInfo(loader, mapping));
        }

        WebServer.jsonResponse(context, infos);
    }

    @OpenApi(
            path = "/versions/loader",
            methods = { HttpMethod.GET },
            description = "Lists all Flint Loader versions",
            versions = "v1",
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JSON arrays of Flint Loader versions",
                            content = { @OpenApiContent(from = LoaderVersion[].class )}
                    )
            }
    )
    private void getLoaders(@NotNull Context context) {
        WebServer.jsonResponse(context, versionsDatabase.getLoaders());
    }

    @OpenApi(
            path = "/versions/intermediary/game",
            methods = { HttpMethod.GET },
            description = "Lists all intermediary mappings for a game version",
            versions = "v1",
            pathParams = {
                    @OpenApiParam(
                            name = "game",
                            description = "The game version you want to list intermediary mappings for",
                            required = true
                    )
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JSON arrays of intermediary mapping versions",
                            content = { @OpenApiContent(from = IntermediaryVersion[].class )}
                    )
            }
    )
    private void getIntermediaryFiltered(@NotNull Context context) {
        String gameVersion = context.pathParam("gameVersion");
        WebServer.jsonResponse(context, versionsDatabase.getIntermediary().stream().filter(v -> v.getVersion().equals(gameVersion)).toList());
    }

    @OpenApi(
            path = "/versions/intermediary",
            methods = { HttpMethod.GET },
            description = "Lists all intermediary mappings",
            versions = "v1",
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JSON arrays of intermediary mapping versions",
                            content = { @OpenApiContent(from = IntermediaryVersion[].class )}
                    )
            }
    )
    private void getIntermediary(@NotNull Context context) {
        WebServer.jsonResponse(context, versionsDatabase.getIntermediary());
    }

    @OpenApi(
            path = "/versions/yarn/game",
            methods = { HttpMethod.GET },
            description = "Lists all yarn mappings for a game version",
            versions = "v1",
            pathParams = {
                    @OpenApiParam(
                            name = "game",
                            description = "The game version you want to list yarn mappings for",
                            required = true
                    )
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JSON arrays of yarn mapping versions",
                            content = { @OpenApiContent(from = YarnVersion[].class )}
                    )
            }
    )
    private void getFilteredYarnVersions(@NotNull Context context) {
        String gameVersion = context.pathParam("gameVersion");

        WebServer.jsonResponse(context, versionsDatabase.getMappings().stream().filter(v -> v.getGameVersion().equals(gameVersion)).toList());
    }

    @OpenApi(
            path = "/versions/yarn",
            methods = { HttpMethod.GET },
            description = "Lists all yarn mappings",
            versions = "v1",
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JSON arrays of yarn mapping versions",
                            content = { @OpenApiContent(from = YarnVersion[].class )}
                    )
            }
    )
    private void getYarnVersions(@NotNull Context context) {
        WebServer.jsonResponse(context, versionsDatabase.getMappings());
    }

    @OpenApi(
            path = "/versions/game",
            methods = { HttpMethod.GET },
            description = "Lists all supported game versions",
            versions = "v1",
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JSON arrays of game versions",
                            content = { @OpenApiContent(from = GameVersion[].class )}
                    )
            }
    )
    private void getGameVersions(@NotNull Context context) {
        WebServer.jsonResponse(context, versionsDatabase.getGame());
    }

    @OpenApi(
            path = "/versions",
            methods = { HttpMethod.GET },
            description = "Lists all supported versions",
            versions = "v1",
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "JSON Arrays of the game, loader, mappings and yarn versions\r\nWARNING: THIS IS A LARGE RESULT",
                            content = { @OpenApiContent(from = AllVersions.class )}
                    )
            }
    )
    private void getAllVersions(@NotNull Context context) {
        WebServer.jsonResponse(context, new AllVersions(versionsDatabase.getGame(), versionsDatabase.getLoaders(), versionsDatabase.getMappings(), versionsDatabase.getIntermediary(), versionsDatabase.getInstallers()));
    }

    private LoaderInfo buildLoaderInfo(LoaderVersion loaderVersion, IntermediaryVersion version) {
        String[] split = loaderVersion.getMaven().split(":");
        String path = String.format("%s/%s/%s", split[0].replaceAll("\\.", "/"), split[1], split[2]);
        String filename = String.format("%s-%s.json", split[1], split[2]);
        String url = String.format("%s%s/%s", Constants.FLINT_MAVEN, path, filename);

        JsonObject launcherMeta = null;

        try {
            launcherMeta = RemoteJsonReader.readJsonFromUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (launcherMeta == null) {
            launcherMeta = new JsonObject();
        }

        return new LoaderInfo(loaderVersion, version, launcherMeta);
    }
}
