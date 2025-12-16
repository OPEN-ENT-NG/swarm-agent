package fr.cgi.learninghub.swarm.cluster;

import fr.cgi.learninghub.swarm.deployment.BaseDeployment;
import fr.cgi.learninghub.swarm.deployment.DatabaseSecret;
import fr.cgi.learninghub.swarm.entity.Deployment;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;

import java.util.HashMap;
import java.util.UUID;

@Group("fr.cgi.learninghub.swarm")
@Version("v1")
@Kind("Wordpress")
public class WordpressCustomResource extends CustomResource<BaseDeployment.BaseDeploymentSpec, Void> implements Namespaced {
    private static final String ADMIN_USER_SITE_SPEC = "adminEmail";
    private static final String ADMIN_PASSWORD_SITE_SPEC = "adminPassword";
    private final String uuid = UUID.randomUUID().toString();
    private final Deployment deployment;

    public WordpressCustomResource(Deployment deployment, DatabaseSecret dbSecret) {
        this.deployment = deployment;

        setMetadata(metadata());
        var spec = new BaseDeployment.BaseDeploymentSpec(deployment, dbSecret);
        var siteSpecs = spec.siteSpecs();
        siteSpecs.put(ADMIN_USER_SITE_SPEC, deployment.getService().getAdminUser());
        siteSpecs.put(ADMIN_PASSWORD_SITE_SPEC, deployment.getService().getAdminPassword());
        spec.setSiteSpecs(siteSpecs);
        setSpec(spec);
    }


    private ObjectMeta metadata() {
        var annotations = new HashMap<String, String>();
        annotations.put(BaseDeployment.AnnotationFields.CREATED, BaseDeployment.formatDate(deployment.getService().getCreated()));
        annotations.put(BaseDeployment.AnnotationFields.DELETION_SCHEDULED_ON, BaseDeployment.formatDate(deployment.getService().getDeletionDate()));

        return new ObjectMetaBuilder()
                .withName(deployment.getService().getServiceName())
                .withUid(uuid)
                .withAnnotations(annotations)
                .build();
    }
}
