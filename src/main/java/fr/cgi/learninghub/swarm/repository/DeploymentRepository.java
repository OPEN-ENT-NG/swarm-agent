package fr.cgi.learninghub.swarm.repository;

import fr.cgi.learning.hub.swarm.common.enums.State;
import fr.cgi.learninghub.swarm.entity.Deployment;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Date;
import java.util.List;

@ApplicationScoped
public class DeploymentRepository implements PanacheRepository<Deployment> {
    public Uni<List<Deployment>> listDeploymentsWhereServiceInState(List<State> states) {
        return list("service.state IN ?1", states);
    }

    public Uni<Deployment> findByServiceId(String serviceId) {
        return find("service.id", serviceId).firstResult();
    }

    public Uni<Deployment> setError(Deployment deployment, String error) {
        deployment.setError(error);
        return Panache.withTransaction(() -> persist(deployment));
    }
}
