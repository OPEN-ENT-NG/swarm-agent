package fr.cgi.learninghub.swarm.deployment;

public class DatabaseServiceConfig {
    private String dbName;
    private String dbUser;
    private DatabaseSecret dbSecret;

    public DatabaseServiceConfig() {
    }

    public DatabaseServiceConfig(String dbName, String dbUser, DatabaseSecret dbSecret) {
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbSecret = dbSecret;
    }

    public String dbName() {
        return dbName;
    }

    public DatabaseServiceConfig setDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    public DatabaseSecret dbSecret() {
        return dbSecret;
    }

    public DatabaseServiceConfig setDbSecret(DatabaseSecret dbSecret) {
        this.dbSecret = dbSecret;
        return this;
    }

    public String dbUser() {
        return dbUser;
    }

    public DatabaseServiceConfig setDbUser(String dbUser) {
        this.dbUser = dbUser;
        return this;
    }
}
