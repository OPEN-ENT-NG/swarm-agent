package fr.cgi.learninghub.swarm.deployment;

import fr.cgi.learninghub.swarm.entity.Deployment;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.client.CustomResource;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class BaseDeployment {
    public static final String API_VERSION = "fr.cgi.learninghub.swarm/v1";

    protected final Deployment deployment;
    protected final List<Deployment> deployments;

    protected DatabaseSecret dbSecret;
    protected String uuid;

    public BaseDeployment(Deployment deployment) {
        this.deployments = null;
        this.deployment = deployment;
        this.uuid = UUID.randomUUID().toString();
    }

    protected BaseDeployment(List<Deployment> deployments) {
        this.deployment = null;
        this.deployments = deployments;
        this.uuid = UUID.randomUUID().toString();
    }

    public static String formatDate(Date date) {
        var pattern = "dd/MM/yyyy HH:mm:ss";
        var df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    public DatabaseSecret getDbSecret() {
        return dbSecret;
    }

    public BaseDeployment setDbSecret(DatabaseSecret dbSecret) {
        this.dbSecret = dbSecret;
        return this;
    }

    public abstract CustomResource<?, ?> getManifest();

    public abstract OwnerReference getOwnerReference(CustomResource<?, ?> crd);

    public static class AnnotationFields {
        public static final String CREATED = "created";
        public static final String DELETION_SCHEDULED_ON = "deletion_scheduled_on";
    }

    public static class SiteSpecFields {
        public static final String SPEC_NAME = "site";
        public static final String HOST = "host";
        public static final String PATH = "path";
        public static final String PORT = "PORT";
        public static final String PATH_TYPE = "path_type";
        public static final String NAME = "name";
        public static final String ID = "id";
        public static final String NUMBER = "number";
        public static final String SERVICE = "service";
        public static final String BACKEND = "backend";
    }

    public static class DatabaseSpecFields {
        public static final String SPEC_NAME = "database";
        public static final String PORT = "port";
        public static final String HOST = "host";
        public static final String NAME = "name";
        public static final String PASSWORD_SECRET_KEY = "passwordSecretKey";
        public static final String PASSWORD_SECRET_NAME = "passwordSecretName";
        public static final String USER = "user";
    }

    public static class StorageSpecFields {
        public static final String SPEC_NAME = "storage";
        public static final String SIZE = "size";
        public static final String STORAGE_CLASS_NAME = "storageClassName";
        public static final String ACCESS_MODES = "accessModes";
    }

    static class MetadataFields {
        public static final String NAME = "name";
        public static final String LABELS = "labels";
        public static final String ANNOTATIONS = "annotations";
    }

    static class IngressSpecFields {
        public static final String SPEC_NAME = "ingressClassName";
        public static final String PATH = "path";
        public static final String NAME = "name";
        public static final String ID = "id";
    }

    static class TlsSpecFields {
        public static final String SPEC_NAME = "tls";
        public static final String HOSTS = "hosts";
        public static final String SECRET_NAME = "secretName";
    }

    static class RulesSpecFields {
        public static final String SPEC_NAME = "rules";
        public static final String HOST = "host";
        public static final String PATHS = "paths";
        public static final String HTTP = "http";
    }

    public static final class BaseDeploymentSpec implements Serializable {

        private final Deployment deployment;
        private final DatabaseSecret dbSecret;

        private HashMap<String, Object> site;
        private HashMap<String, Object> database;
        private HashMap<String, Object> storage;

        public BaseDeploymentSpec(Deployment deployment, DatabaseSecret dbSecret) {
            this.deployment = deployment;
            this.dbSecret = dbSecret;
            this.site = siteSpecs();
            this.database = databaseSpecs();
            this.storage = null;
        }

        public HashMap<String, Object> databaseSpecs() {
            var spec = new HashMap<String, Object>();
            spec.put(DatabaseSpecFields.HOST, deployment.getDatabase().getHost());
            spec.put(DatabaseSpecFields.PORT, deployment.getDatabase().getPort());
            spec.put(DatabaseSpecFields.NAME, deployment.getDbName());
            spec.put(DatabaseSpecFields.USER, deployment.getDbUser());
            spec.put(DatabaseSpecFields.PASSWORD_SECRET_NAME, dbSecret.name());
            spec.put(DatabaseSpecFields.PASSWORD_SECRET_KEY, DatabaseSecret.PASSWORD);

            return spec;
        }

        public HashMap<String, Object> siteSpecs() {
            var spec = new HashMap<String, Object>();
            spec.put(SiteSpecFields.HOST, deployment.getPublicHostname());
            spec.put(SiteSpecFields.ID, deployment.getService().getId());
            spec.put(SiteSpecFields.NAME, deployment.getService().getServiceName());
            spec.put(SiteSpecFields.PATH, deployment.getPath());

            return spec;
        }

        public BaseDeploymentSpec setSiteSpecs(HashMap<String, Object> siteSpecs) {
            this.site = siteSpecs;
            return this;
        }

        public BaseDeploymentSpec setDatabaseSpecs(HashMap<String, Object> databaseSpecs) {
            this.database = databaseSpecs;
            return this;
        }

        public BaseDeploymentSpec setStorageSpecs(HashMap<String, Object> storageSpecs) {
            this.storage = storageSpecs;
            return this;
        }

        public HashMap<String, Object> getSite() {
            return site;
        }

        public HashMap<String, Object> getDatabase() {
            return database;
        }

        public HashMap<String, Object> getStorage() {
            return storage;
        }
    }
}
