/**
 * This file is part of flint-meta and is licensed under the MIT License
 */
package net.flintloader.meta.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import net.flintloader.meta.Constants;
import net.flintloader.meta.web.routes.v1.VersionsRoute;

/**
 * @author HypherionSA
 * Main API Server Entrypoint
 */
public class WebServer {

    public static Javalin javalin;
    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Javalin create() {
        if (javalin != null)
            javalin.stop();

        javalin = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.plugins.enableCors(cors -> cors.add(CorsPluginConfig::anyHost));
            config.plugins.register(buildOpenApi());

            SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
            config.plugins.register(new SwaggerPlugin(swaggerConfiguration));
        });

        javalin.get("/", (ctx) -> ctx.redirect("/swagger"));

        new VersionsRoute();

        return javalin;
    }

    public static void start() {
        assert javalin == null;
        create().start(5806);
    }

    /**
     * This portion of code is from https://github.com/FabricMC/fabric-meta, licensed under Apache 2.0.
     * See License here: https://github.com/FabricMC/fabric-meta/blob/master/LICENSE
     */
    public static void jsonResponse(Context ctx, Object object) {
        if (object == null) {
            object = new Object();
            ctx.status(400);
        }

        String response = GSON.toJson(object);
        ctx.contentType("application/json").header(Header.CACHE_CONTROL, "public, max-age=60").result(response);
    }
    // End apache-2.0 code

    private static OpenApiPlugin buildOpenApi() {
        return new OpenApiPlugin(
                new OpenApiPluginConfiguration()
                        .withDocumentationPath("/openapi")
                        .withDefinitionConfiguration((version, definition) -> definition
                                .withOpenApiInfo((openApiInfo) -> {
                                    openApiInfo.setTitle("Flint Loader Meta");
                                    openApiInfo.setVersion("1.0.0");
                                })
                                .withServer((openApiServer) -> {
                                    openApiServer.setUrl((Constants.BASE_URL + "/" + version + "/"));
                                    openApiServer.setDescription("Main Server");
                                })
                        )
        );
    }
}