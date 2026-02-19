package fr.cgi.learninghub.swarm.deployment;

import fr.cgi.learning.hub.swarm.common.entities.Service;
import fr.cgi.learning.hub.swarm.common.enums.State;
import fr.cgi.learning.hub.swarm.common.enums.Type;
import fr.cgi.learninghub.swarm.entity.Database;
import fr.cgi.learninghub.swarm.entity.Deployment;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@QuarkusTest
public class WordpressTest {

    public static final String SERVICE_ID = "0fccdff1-dbc2-47fd-96bd-27236be9c43f";
    public static final String SERVICE_NAME = "wp-doe";
    public static final String SERVICE_PATH = "/wp-" + SERVICE_ID;
    public static final String DB_NAME = "db-name";
    public static final String DB_USER = "db-user";
    public static final String DB_SECRET_NAME = "db-secret-wordpress";
    private static final String CREATION_DATE = "15/09/2024 14:23:31";
    private static final String DELETION_DATE = "01/10/2024 00:00:00";
    private static final String DB_HOST = "localhost";
    private static final Integer DB_PORT = 3306;
    private static Service service;
    private static Database database;
    private static Deployment deployment;
    private static Wordpress wordpress;
    private static DatabaseSecret dbSecret;


    @BeforeAll
    public static void setUp() throws ParseException {
        dbSecret = new DatabaseSecret("wordpress", "password");
        service = new Service()
                .setId(SERVICE_ID)
                .setServiceName(SERVICE_NAME)
                .setState(State.SCHEDULED)
                .setFirstName("John")
                .setLastName("DOE")
                .setCreated(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(CREATION_DATE))
                .setDeletionDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(DELETION_DATE))
                .setStructureId("a74eb656-42e3-4b22-a841-d5dc62f93c4a")
                .setType(Type.WORDPRESS);
        database = new Database()
                .setDefaultDB("defaultdb")
                .setEnabled(true)
                .setHost(DB_HOST)
                .setPort(DB_PORT)
                .setUsername("username")
                .setPassword("password");
        deployment = new Deployment()
                .setService(service)
                .setDbName(DB_NAME)
                .setDbUser(DB_USER)
                .setDatabase(database)
                .setPath(SERVICE_PATH);
        wordpress = new Wordpress(deployment);
        wordpress.setDbSecret(dbSecret);
    }

    @Test
    @DisplayName("CRD apiVersion should be " + BaseDeployment.API_VERSION)
    public void testAPIVersion() {
        var crd = wordpress.getManifest();
        Assertions.assertEquals(BaseDeployment.API_VERSION, crd.getApiVersion());
    }

    @Test
    @DisplayName("CRD kind should be Wordpress")
    public void testKind() {
        var cr = wordpress.getManifest();

        Assertions.assertEquals("Wordpress", cr.getKind());
    }

    @Test
    @DisplayName("CRD metadata should contains created and deletion_scheduled with the right data")
    public void testMetadata() {
        var cr = wordpress.getManifest();
        var metadata = cr.getMetadata();
        // Created field
        Assertions.assertTrue(metadata.getAnnotations().containsKey(BaseDeployment.AnnotationFields.CREATED));
        Assertions.assertEquals(CREATION_DATE, metadata.getAnnotations().get(BaseDeployment.AnnotationFields.CREATED));

        // Deletion field
        Assertions.assertTrue(metadata.getAnnotations().containsKey(BaseDeployment.AnnotationFields.DELETION_SCHEDULED_ON));
        Assertions.assertEquals(DELETION_DATE, metadata.getAnnotations().get(BaseDeployment.AnnotationFields.DELETION_SCHEDULED_ON));
    }

    @Test
    @DisplayName("CRD spec should contains a site nested spec with the right data")
    public void testSiteSpec() {
        var cr = wordpress.getManifest();
        var spec = (BaseDeployment.BaseDeploymentSpec) cr.getSpec();

        var siteSpec = (Map<String, Object>) spec.getSite();
        // Check ID data
        Assertions.assertTrue(siteSpec.containsKey(BaseDeployment.SiteSpecFields.ID));
        Assertions.assertEquals(SERVICE_ID, siteSpec.get(BaseDeployment.SiteSpecFields.ID));

        // Check NAME data
        Assertions.assertTrue(siteSpec.containsKey(BaseDeployment.SiteSpecFields.NAME));
        Assertions.assertEquals(SERVICE_NAME, siteSpec.get(BaseDeployment.SiteSpecFields.NAME));

        // Check PATH data
        Assertions.assertTrue(siteSpec.containsKey(BaseDeployment.SiteSpecFields.PATH));
        Assertions.assertEquals(SERVICE_PATH, siteSpec.get(BaseDeployment.SiteSpecFields.PATH));
    }

    @Test
    @DisplayName("CRD spec should contains a database nested spec with the right data")
    public void testDatabaseSpec() {
        var cr = wordpress.getManifest();
        var spec = (BaseDeployment.BaseDeploymentSpec) cr.getSpec();

        var dbSpec = (Map<String, Object>) spec.getDatabase();
        // Check URL data
        Assertions.assertTrue(dbSpec.containsKey(BaseDeployment.DatabaseSpecFields.HOST));
        Assertions.assertEquals(DB_HOST, dbSpec.get(BaseDeployment.DatabaseSpecFields.HOST)); //FIXME

        // Check PORT data
        Assertions.assertTrue(dbSpec.containsKey(BaseDeployment.DatabaseSpecFields.PORT));
        Assertions.assertEquals(DB_PORT, dbSpec.get(BaseDeployment.DatabaseSpecFields.PORT)); ///FIXME

        // Check NAME data
        Assertions.assertTrue(dbSpec.containsKey(BaseDeployment.DatabaseSpecFields.NAME));
        Assertions.assertEquals(DB_NAME, dbSpec.get(BaseDeployment.DatabaseSpecFields.NAME));

        // Check USER data
        Assertions.assertTrue(dbSpec.containsKey(BaseDeployment.DatabaseSpecFields.USER));
        Assertions.assertEquals(DB_USER, dbSpec.get(BaseDeployment.DatabaseSpecFields.USER));

        // Check passwordSecretName data
        Assertions.assertTrue(dbSpec.containsKey(BaseDeployment.DatabaseSpecFields.PASSWORD_SECRET_NAME));
        Assertions.assertEquals(DB_SECRET_NAME, dbSpec.get(BaseDeployment.DatabaseSpecFields.PASSWORD_SECRET_NAME));

        // Check passwordSecretKey data
        Assertions.assertTrue(dbSpec.containsKey(BaseDeployment.DatabaseSpecFields.PASSWORD_SECRET_KEY));
        Assertions.assertEquals(DatabaseSecret.PASSWORD, dbSpec.get(BaseDeployment.DatabaseSpecFields.PASSWORD_SECRET_KEY));
    }

    @Test
    @DisplayName("CRD spec should contains a storage nested spec with the default data")
    public void testStorageSpecDefaults() {
        var cr = wordpress.getManifest();
        var spec = (BaseDeployment.BaseDeploymentSpec) cr.getSpec();

        var storageSpec = (Map<String, Object>) spec.getStorage();
        Assertions.assertNotNull(storageSpec);

        Assertions.assertTrue(storageSpec.containsKey(BaseDeployment.StorageSpecFields.SIZE));
        Assertions.assertEquals("1Gi", storageSpec.get(BaseDeployment.StorageSpecFields.SIZE));

        Assertions.assertTrue(storageSpec.containsKey(BaseDeployment.StorageSpecFields.ACCESS_MODES));
        Assertions.assertEquals(List.of("ReadWriteOnce"), storageSpec.get(BaseDeployment.StorageSpecFields.ACCESS_MODES));

        Assertions.assertFalse(storageSpec.containsKey(BaseDeployment.StorageSpecFields.STORAGE_CLASS_NAME));
    }
}
