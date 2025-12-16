package fr.cgi.learninghub.swarm.service;

import fr.cgi.learning.hub.swarm.common.entities.Service;
import fr.cgi.learning.hub.swarm.common.enums.Type;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Objects;

@Singleton
public class DatabaseFactory {

    private final PrestashopDatabaseService prestashopDatabaseService;
    private final WordpressDatabaseService wordpressDatabaseService;

    @Inject
    public DatabaseFactory(PrestashopDatabaseService prestashopDatabaseService, WordpressDatabaseService wordpressDatabaseService) {
        this.prestashopDatabaseService = prestashopDatabaseService;
        this.wordpressDatabaseService = wordpressDatabaseService;
    }

    public DatabaseService getDatabaseService(Service service) {
        if (Objects.requireNonNull(service.getType()) == Type.WORDPRESS) {
            return wordpressDatabaseService;
        }
        
        return prestashopDatabaseService;
    }
}
