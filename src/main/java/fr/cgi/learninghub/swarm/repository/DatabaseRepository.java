package fr.cgi.learninghub.swarm.repository;

import fr.cgi.learninghub.swarm.entity.Database;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Random;

@ApplicationScoped
public class DatabaseRepository implements PanacheRepository<Database> {

    public Uni<Database> getRandomDatabase() {
        return this.list("enabled", true)
                .onItem().transform(databases -> {
                    var rand = new Random();
                    return databases.get(rand.nextInt(databases.size()));
                });
    }
}
