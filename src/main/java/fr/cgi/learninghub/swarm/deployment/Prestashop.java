package fr.cgi.learninghub.swarm.deployment;


import fr.cgi.learninghub.swarm.cluster.PrestashopCustomResource;
import fr.cgi.learninghub.swarm.entity.Deployment;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.client.CustomResource;

public class Prestashop extends BaseDeployment {

    public Prestashop(Deployment deployment) {
        super(deployment);
    }

    @Override
    public CustomResource<?, ?> getManifest() {
        return new PrestashopCustomResource(deployment, dbSecret);
    }

    @Override
    public OwnerReference getOwnerReference(CustomResource<?, ?> crd) {
        return new OwnerReferenceBuilder()
                .withUid(crd.getMetadata().getUid())
                .withApiVersion(crd.getApiVersion())
                .withName(crd.getMetadata().getName())
                .withKind(crd.getKind())
                .build();
    }

    @Override
    public String toString() {
        return "Prestashop{" +
                "deployment=" + deployment +
                ", deployments=" + deployments +
                ", dbSecret=" + dbSecret +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
