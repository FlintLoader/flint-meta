/**
 * This file is part of flint-meta and is licensed under the MIT License
 */
package net.flintloader.meta;

import net.flintloader.meta.database.VersionsDatabase;
import net.flintloader.meta.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author HypherionSA
 * Main Server Entry Point
 */
public class FlintMeta {

    public static final Logger LOGGER = LoggerFactory.getLogger(FlintMeta.class);
    public static final VersionsDatabase versionsDatabase = new VersionsDatabase();
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) throws IOException {
        versionsDatabase.generateDatabase();
        WebServer.start();

        executor.scheduleAtFixedRate(() -> {
            try {
                versionsDatabase.generateDatabase();
            } catch (Exception e) {
                LOGGER.error("Failed to update versions database", e);
            }
        }, 2, 2, TimeUnit.MINUTES);
    }
}
