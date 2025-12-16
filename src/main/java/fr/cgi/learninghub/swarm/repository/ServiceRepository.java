package fr.cgi.learninghub.swarm.repository;

import fr.cgi.learning.hub.swarm.common.entities.Service;
import fr.cgi.learning.hub.swarm.common.enums.State;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ServiceRepository implements PanacheRepository<Service> {

    public Uni<List<Service>> listServicesByState(State state) {
        return list("state", state);
    }

    public Uni<Service> updateState(Service service, State newState) {
        Log.infov("Updating service {0} to state {1}", service.getServiceName(), newState);
        service.setState(newState);
        return Panache.withTransaction(() -> persist(service));
    }

    public Uni<Service> updateOverrideCredentials(Service service, String userLogin, String userPassword) {
        return Panache.withTransaction(() -> {
            service.setOverrideAdminUser(userLogin)
                    .setAdminPassword(userPassword);

            return persist(service).log();
        });
    }

    public Uni<Service> updateOwnerAdminUserCredentials(Service service, String ownerAdminUser, String ownerAdminUserPassword) {
        return Panache.withTransaction(() -> {
            service.setOwnerAdminUser(ownerAdminUser)
                    .setOwnerAdminPassword(ownerAdminUserPassword);
            return persist(service).log();
        });
    }
}
