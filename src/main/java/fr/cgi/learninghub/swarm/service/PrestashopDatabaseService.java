package fr.cgi.learninghub.swarm.service;

import fr.cgi.learninghub.swarm.entity.Deployment;
import fr.cgi.learninghub.swarm.exceptions.NullDatabaseException;
import io.quarkus.elytron.security.common.BcryptUtil;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.inject.Singleton;

import java.util.Objects;

@Singleton
public class PrestashopDatabaseService extends DatabaseService {

    @Override
    public Uni<Deployment> deactivateService(Deployment deployment) {
        if (Objects.isNull(deployment.getDatabase())) {
            throw new NullDatabaseException();
        }

        Log.infov("Deactivating service {1}", deployment.getService());
        var transientRecord = new TransientRecord(getDeploymentPool(deployment), deployment);
        return transientRecord.pool().preparedQuery("UPDATE ps_employee SET id_profile = 0 WHERE id_employee = 1;")
                .execute()
                .replaceWith(deployment);
    }

    @Override
    public Uni<Deployment> reactivateService(Deployment deployment) {
        if (Objects.isNull(deployment.getDatabase())) {
            throw new NullDatabaseException();
        }

        Log.infov("Reactivating service {1}", deployment.getService());
        var transientRecord = new TransientRecord(getDeploymentPool(deployment), deployment);
        return transientRecord.pool().preparedQuery("UPDATE ps_employee SET id_profile = 1 WHERE id_employee = 1;")
                .execute()
                .replaceWith(deployment);
    }

    public Uni<Deployment> createOwnerAdminUser(Deployment deployment, String newAdminUsername, String newAdminPassword) {
        Log.info("Creating owner admin user for Prestashop");

        if (Objects.isNull(deployment.getDatabase())) {
            throw new NullDatabaseException();
        }

        var transientRecord = new TransientRecord(getDeploymentPool(deployment), deployment);

        // Hash password with Bcrypt
        String hashedPassword = BcryptUtil.bcryptHash(newAdminPassword);

        return transientRecord.pool().preparedQuery(
                        "INSERT INTO ps_employee (id_profile, id_lang, lastname, firstname, email, passwd, active, stats_date_from, stats_date_to, default_tab, bo_theme) " +
                                "VALUES (1, 1, ?, ?, ?, ?, 1, CURRENT_DATE(), CURRENT_DATE(), 1, 'default')")
                .execute(Tuple.of(newAdminUsername, newAdminUsername, String.format("%s@%s", newAdminUsername, clusterConfiguration.getPsEmailHostname()), hashedPassword))
                .log()
                .chain(ignored ->
                        transientRecord.pool().preparedQuery("INSERT INTO ps_employee_shop (id_employee, id_shop) VALUES (LAST_INSERT_ID(), 1)")
                                .execute()
                                .log()
                )
                .replaceWith(deployment);
    }

}
