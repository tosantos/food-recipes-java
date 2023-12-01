package org.olivetree.recipes.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.olivetree.recipes.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.net.URI;
import java.util.logging.LogManager;

public class RecipeServer {
    // Just to normalize the logs to use SL4J Simple
    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    private static final Logger LOG = LoggerFactory.getLogger(RecipeServer.class);
    public static final String BASE_URI = "http://localhost:8080";

    public static void main(String[] args) throws IOException {
        LOG.info("Starting HTTP server");

        RecipeRepository recipeRepository = RecipeRepository.openRecipeRepository();

        createHttpServer(recipeRepository);
    }

    // Also used for test purposes
    public static HttpServer createHttpServer(RecipeRepository recipeRepository) {
        ResourceConfig config = new ResourceConfig()
                .register(new RecipeResource(recipeRepository))
                .register(new RecipeSearchResource(recipeRepository));

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }
}
