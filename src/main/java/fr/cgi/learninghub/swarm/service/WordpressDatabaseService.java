package fr.cgi.learninghub.swarm.service;

import fr.cgi.learninghub.swarm.entity.Deployment;
import fr.cgi.learninghub.swarm.exceptions.NullDatabaseException;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.inject.Singleton;

import java.util.Objects;

@Singleton
public class WordpressDatabaseService extends DatabaseService {

    @Override
    public Uni<Deployment> deactivateService(Deployment deployment) {
        if (Objects.isNull(deployment.getDatabase())) {
            throw new NullDatabaseException();
        }

        Log.infov("Deactivating service {1}", deployment.getService());
        var transientRecord = new TransientRecord(getDeploymentPool(deployment), deployment);
        return transientRecord.pool().preparedQuery("DELETE FROM wp_usermeta WHERE user_id = 1 AND meta_key = 'wp_capabilities';")
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
        return transientRecord.pool().preparedQuery("INSERT INTO wp_usermeta (user_id, meta_key, meta_value) VALUES (1, 'wp_capabilities', 'a:1:{s:13:\"administrator\";b:1;}');")
                .execute()
                .replaceWith(deployment);
    }

    public Uni<Deployment> createOwnerAdminUser(Deployment deployment, String newAdminUsername, String newAdminPassword) {
        Log.info("Creating owner admin user for WP");

        if (Objects.isNull(deployment.getDatabase())) {
            throw new NullDatabaseException();
        }

        var transientRecord = new TransientRecord(getDeploymentPool(deployment), deployment);
        return transientRecord.pool().preparedQuery("INSERT INTO wp_users (user_login, user_pass, user_nicename, user_email, user_registered, user_status, display_name) VALUES (?, MD5(?), ?, ?, NOW(), 0, ?)")
                .execute(Tuple.of(newAdminUsername, newAdminPassword, newAdminUsername, String.format("%s@%s", newAdminUsername, clusterConfiguration.getWpEmailHostname()), newAdminUsername)).log()
                .chain(ignored ->
                        transientRecord.pool()
                                .preparedQuery("INSERT INTO wp_usermeta (user_id, meta_key, meta_value) VALUES (LAST_INSERT_ID(), 'wp_capabilities', 'a:1:{s:13:\"administrator\";b:1;}')")
                                .execute()
                                .log()
                )
                .replaceWith(deployment);
    }

    public Uni<Boolean> isInstalled(Deployment deployment) {
        if (Objects.isNull(deployment.getDatabase())) {
            return Uni.createFrom().failure(new NullDatabaseException());
        }

        var transientRecord = new TransientRecord(getDeploymentPool(deployment), deployment);

        return transientRecord.pool()
                .preparedQuery("SELECT 1 FROM wp_users LIMIT 1;")
                .execute()
                .onItem().transform(rows -> rows.size() > 0)
                .onFailure().recoverWithItem(false);
    }
}
