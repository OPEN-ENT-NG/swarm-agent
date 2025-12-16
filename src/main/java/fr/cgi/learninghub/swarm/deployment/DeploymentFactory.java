package fr.cgi.learninghub.swarm.deployment;

import fr.cgi.learning.hub.swarm.common.enums.Type;
import fr.cgi.learninghub.swarm.entity.Deployment;
import jakarta.inject.Singleton;

@Singleton
public class DeploymentFactory {

    public static String getServicePrefix(Type type) {
        return switch (type) {
            case PRESTASHOP -> "presta";
            case WORDPRESS -> "wp";
        };
    }

    public static String getAdminPathPrefix(Type type) {
        return switch (type) {
            case PRESTASHOP -> "ps-admin";
            case WORDPRESS -> "wp-admin";
        };
    }

    public BaseDeployment get(Deployment deployment) {
        switch (deployment.getService().getType()) {
            case WORDPRESS -> {
                return new Wordpress(deployment);
            }
            case PRESTASHOP -> {
                return new Prestashop(deployment);
            }
        }

        throw new RuntimeException("Unable to get deployment. Type not found.");
    }
}
