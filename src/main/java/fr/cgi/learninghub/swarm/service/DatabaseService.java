package fr.cgi.learninghub.swarm.service;

import fr.cgi.learninghub.swarm.cluster.ClusterConfiguration;
import fr.cgi.learninghub.swarm.entity.Deployment;
import fr.cgi.learninghub.swarm.exceptions.NullDatabaseException;
import fr.cgi.learninghub.swarm.repository.CredentialsRepository;
import fr.cgi.learninghub.swarm.repository.ServiceRepository;
import fr.cgi.learninghub.swarm.utils.UserUtils;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mysqlclient.MySQLBuilder;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.SslMode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;

import java.util.List;
import java.util.Objects;

@Singleton
public abstract class DatabaseService {

    @Inject
    Vertx vertx;

    @Inject
    ClusterConfiguration clusterConfiguration;

    @Inject
    CredentialsRepository credentialsRepository;

    @Inject
    ServiceRepository serviceRepository;

    protected Pool getDefaultPool(Deployment deployment) {
        return getPool(deployment, deployment.getDatabase().getDefaultDB(), deployment.getDatabase().getUsername(), deployment.getDatabase().getPassword());
    }

    protected Pool getPool(Deployment deployment, String dbName, String user, String password) {
        if (Objects.isNull(deployment.getDatabase())) {
            throw new NullDatabaseException();
        }

        var options = new MySQLConnectOptions()
                .setDatabase(dbName)
                .setHost(deployment.getDatabase().getHost())
                .setSslMode(SslMode.DISABLED)
                .setPort(deployment.getDatabase().getPort())
                .setUser(user)
                .setPassword(password);

        if (deployment.getDatabase().getSsl()) {
            options.setSslMode(SslMode.REQUIRED)
                    .setPemTrustOptions(new PemTrustOptions().addCertValue(Buffer.buffer(deployment.getDatabase().getCaCert())));
        }

        return MySQLBuilder.pool()
                .connectingTo(options)
                .using(vertx)
                .build();
    }

    protected Pool getDeploymentPool(Deployment deployment) {
        return getPool(deployment, deployment.getDbName(), deployment.getDbUser(), deployment.getDbPassword());
    }

    public Uni<Deployment> createDatabaseAndGenerateAuthentication(Deployment deployment) {
        return createDatabase(getDefaultPool(deployment), deployment)
                .onItem().transformToUni(transientRecord -> generateUserAndPassword(transientRecord.pool(), transientRecord.deployment()))
                .onItem().transformToUni(transientRecord -> transientRecord.pool().close().onItem().transformToUni(unused -> Uni.createFrom().item(transientRecord.deployment())));
    }

    public Uni<Deployment> dropDatabaseAndDatabaseUser(Deployment deployment) {
        return dropUser(getDefaultPool(deployment), deployment)
                .onItem().transformToUni(transientRecord -> dropDatabase(transientRecord.pool(), transientRecord.deployment()))
                .onItem().transformToUni(transientRecord -> transientRecord.pool().close().onItem().transformToUni(unused -> Uni.createFrom().item(transientRecord.deployment())));
    }

    private Uni<TransientRecord> dropUser(Pool pool, Deployment deployment) {
        return pool.query(String.format("DROP USER '%s'@'%s';", deployment.getDbUser(), clusterConfiguration.getClusterIp())).execute()
                .onItem().transform(unused -> new TransientRecord(pool, deployment));
    }

    private Uni<TransientRecord> dropDatabase(Pool pool, Deployment deployment) {
        return pool.query(String.format("DROP DATABASE IF EXISTS `%s`;", deployment.getDbName())).execute()
                .onItem().transform(unused -> new TransientRecord(pool, deployment));
    }

    private Uni<TransientRecord> createDatabase(Pool pool, Deployment deployment) {
        var dbName = generateDatabaseName();
        Log.infov("[Service {0}] creating database {1}", deployment.getService().getId(), dbName);
        return pool.query(String.format("CREATE DATABASE `%s`", dbName))
                .execute()
                .onItem().transform(unused -> new TransientRecord(pool, deployment.setDbName(dbName)));
    }

    private Uni<TransientRecord> generateUserAndPassword(Pool pool, Deployment deployment) {
        Log.debugv("[Service {0}] generating user and password", deployment.getService().getId());
        var username = UserUtils.generateUsername();
        var password = UserUtils.generatePassword();
        return pool.query(String.format("CREATE USER '%s'@'%s' IDENTIFIED BY '%s';", username, clusterConfiguration.getClusterIp(), password)).execute()
                .onItem().transformToUni(unused -> {
                    Log.infov("[Service {0}] user {1} created", deployment.getService().getId(), username);
                    return pool.query(String.format("GRANT ALL PRIVILEGES ON %s.* TO '%s'@'%s';", deployment.getDbName(), username, clusterConfiguration.getClusterIp())).execute();
                })
                .onItem().transform(unused -> {
                    Log.infov("[Service {0}] privileges granted for user {1} on database {2}", deployment.getService().getId(), username, deployment.getDbName());
                    return new TransientRecord(pool, deployment.setDbUser(username).setDbPassword(password));
                });
    }

    private String generateDatabaseName() {
        var length = 16;
        List<CharacterRule> rules = List.of(
                // database name is full of lower case
                new CharacterRule(EnglishCharacterData.LowerCase, length)
        );

        return UserUtils.generateRandomValue(length, rules);
    }

    public abstract Uni<Deployment> reactivateService(Deployment deployment);

    public abstract Uni<Deployment> deactivateService(Deployment deployment);

    public abstract Uni<Deployment> createOwnerAdminUser(Deployment deployment, String adminUsername, String adminPassword);

    public abstract Uni<Boolean> isInstalled(Deployment deployment);

    protected record TransientRecord(Pool pool, Deployment deployment) {
    }
}
