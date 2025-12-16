package fr.cgi.learninghub.swarm.entity;

import fr.cgi.learning.hub.swarm.common.entities.Service;
import fr.cgi.learning.hub.swarm.common.enums.State;
import fr.cgi.learninghub.swarm.deployment.DeploymentFactory;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class Deployment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "database_id")
    private Database database;

    @OneToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @Column
    private String path;

    @Column
    private Date created;

    @Column(name = "db_user")
    private String dbUser;

    @Column(name = "db_password")
    private String dbPassword;

    @Column(name = "db_name")
    private String dbName;

    @Column(name = "error", columnDefinition = "text")
    private String error = "";

    private String publicHostname;

    public Deployment() {
        this.created = new Date(); //Use this instead of @CreationTimestamp to avoid data lost in case of multiple persistence
    }

    public static String generatePath(Service service) {
        return String.format("/%s-%s", DeploymentFactory.getServicePrefix(service.getType()), service.getId());
    }

    public String getId() {
        return id;
    }

    public Deployment setId(String id) {
        this.id = id;
        return this;
    }

    public Database getDatabase() {
        return database;
    }

    public Deployment setDatabase(Database database) {
        this.database = database;
        return this;
    }

    public Service getService() {
        return service;
    }

    public Deployment setService(Service service) {
        this.service = service;
        return this;
    }

    public String getPath() {
        return path;
    }

    public Deployment setPath(String path) {
        this.path = path;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    public Deployment setCreated(Date created) {
        this.created = created;
        return this;
    }

    public String getDbUser() {
        return dbUser;
    }

    public Deployment setDbUser(String dbUser) {
        this.dbUser = dbUser;
        return this;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public Deployment setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
        return this;
    }

    public String getDbName() {
        return dbName;
    }

    public Deployment setDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    @Override
    public String toString() {
        return "Deployment{" +
                "id='" + id + '\'' +
                ", database=" + database +
                ", service=" + service +
                ", path='" + path + '\'' +
                ", created=" + created +
                ", dbUser='" + dbUser + '\'' +
                ", dbPassword='" + dbPassword + '\'' +
                ", dbName='" + dbName + '\'' +
                ", publicHostname='" + publicHostname + '\'' +
                '}';
    }

    public String getPublicHostname() {
        return publicHostname;
    }

    public Deployment setPublicHostname(String publicHostname) {
        this.publicHostname = publicHostname;
        return this;
    }

    public Boolean isNotInError() {
        return !List.of(State.DEPLOYMENT_IN_ERROR, State.DELETION_IN_ERROR, State.DEACTIVATION_IN_ERROR, State.REACTIVATION_IN_ERROR, State.RESET_IN_ERROR).contains(getService().getState());
    }


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
