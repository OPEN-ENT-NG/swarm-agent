package fr.cgi.learninghub.swarm.repository;

import fr.cgi.learninghub.swarm.entity.Credentials;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CredentialsRepository implements PanacheRepository<Credentials> {
}
