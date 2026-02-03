package fr.cgi.learninghub.swarm.scheduler;

import fr.cgi.learning.hub.swarm.common.entities.Service;
import fr.cgi.learning.hub.swarm.common.enums.State;
import fr.cgi.learning.hub.swarm.common.enums.Type;
import fr.cgi.learninghub.swarm.cluster.ClusterConfiguration;
import fr.cgi.learninghub.swarm.cluster.NginxIngress;
import fr.cgi.learninghub.swarm.deployment.DatabaseSecret;
import fr.cgi.learninghub.swarm.deployment.DeploymentFactory;
import fr.cgi.learninghub.swarm.entity.Deployment;
import fr.cgi.learninghub.swarm.repository.DatabaseRepository;
import fr.cgi.learninghub.swarm.repository.DeploymentRepository;
import fr.cgi.learninghub.swarm.repository.ServiceRepository;
import fr.cgi.learninghub.swarm.service.DatabaseFactory;
import fr.cgi.learninghub.swarm.utils.UserUtils;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class

AgentScheduler {

    @Inject
    Vertx vertx;

    @Inject
    ClusterConfiguration clusterConfiguration;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    DeploymentRepository deploymentRepository;

    @Inject
    DatabaseRepository databaseRepository;

    @Inject
    DatabaseFactory databaseFactory;

    @Inject
    DeploymentFactory factory;

    @Inject
    KubernetesClient k8sClient;

    public static final String PRESTASHOP = "prestashop";
    public static final String WORDPRESS = "wordpress";

    @Scheduled(cron = "{swarm.agent.cron}")
    @WithSession
    public Uni<Void> scheduled() {
        // 1. Deploy services
        return deployServices()
                // 2. Delete deployments
                .chain(() -> deploymentRepository.listDeploymentsWhereServiceInState(List.of(State.DELETION_SCHEDULED)).onItem().transformToUni(this::deleteDeployments))
                // 3. Reset deployments
                .chain(() -> deploymentRepository.listDeploymentsWhereServiceInState(List.of(State.RESET_SCHEDULED)).onItem().transformToUni(this::resetDeployments))
                // 4. Deactivate services
                .chain(() -> deploymentRepository.listDeploymentsWhereServiceInState(List.of(State.DEACTIVATION_SCHEDULED)).onItem().transformToUni(this::deactivateDeployments))
                // 5. Reactivate services
                .chain(() -> deploymentRepository.listDeploymentsWhereServiceInState(List.of(State.REACTIVATION_SCHEDULED)).onItem().transformToUni(this::reactivateDeployments))
                // 6. Recheck if the pod is ready and switch to deployed
                .chain(() -> deploymentRepository.listDeploymentsWhereServiceInState(List.of(State.IN_PROGRESS)).onItem().transformToUni(this::checkInProgressDeployments))
                // 7. Recheck if the pod is ready and switch to deployed
                .chain(() -> deploymentRepository.listDeploymentsWhereServiceInState(List.of(State.RESET_IN_PROGRESS)).onItem().transformToUni(this::checkInProgressDeployments));
    }

    private Uni<Void> checkInProgressDeployments(List<Deployment> deployments) {
        return keepReadyDeployments(deployments).chain(this::createIngress).chain(this::removeDeploymentsInError)
                .chain(this::updateServicesStateToDeployed)
                .replaceWithVoid();
    }

    private Uni<Void> reactivateDeployments(List<Deployment> deployments) {
        // 1. Update service state to deactivation in progress
        return this.updateServiceStateToReactivationInProgress(deployments)
                // 2. reactivate services
                .chain(this::reactivateServices)
                // 3. remove Ingress with 403 on admin page
                .chain(this::deleteIngress)
                .chain(this::removeDeploymentsInError)
                .chain(this::createIngress)
                .chain(this::removeDeploymentsInError)
                // 3. update service state to disabled
                .chain(this::updateServicesStateToDeployed)
                .replaceWithVoid();
    }

    private Uni<Void> deactivateDeployments(List<Deployment> deployments) {
        // 1. Update service state to deactivation in progress
        return this.updateServiceStateToDeactivationInProgress(deployments)
                // 2. deactivate services
                .chain(this::deactivateServices)
                .chain(this::removeDeploymentsInError)
                // 3. update service state to disabled
                .chain(this::updateServiceStateToDisabled)
                // 4. Ingress with 403 on admin page
                .chain(this::deleteIngress)
                .chain(this::removeDeploymentsInError)
                .chain(this::createIngress)
                .chain(this::removeDeploymentsInError)
                .replaceWithVoid();
    }

    private Uni<Void> resetDeployments(List<Deployment> deployments) {
        // 1. Update services state to RESET_IN_PROGRESS
        return this.updateServiceStateToResetInProgress(deployments)
                // 2. Delete service from kubernetes
                .chain(this::deleteFromK8s)
                .chain(this::removeDeploymentsInError)
                .chain(this::deleteIngress)
                .chain(this::removeDeploymentsInError)
                // 3. Delete remote database user and database
                .chain(this::cleanDatabase)
                .chain(this::removeDeploymentsInError)
                // 4. Delete deployments
                .chain(this::cleanUpDeployments)
                // 5. Recreate deployments
                .chain(services -> this.createDeploymentEntitiesAndUpdateServiceState(services, State.RESET_IN_PROGRESS))
                // 6. Find the Deployment database and persist it
                .chain(this::findDeploymentDatabase)
                // 7. Based on the Deployment database configuration, create the remote user with password, create the database
                .chain(this::createRemoteDatabase)
                .chain(this::removeDeploymentsInError)
                // 8. Create CRD and send CRD to Kubernetes cluster
                .chain(this::deployToK8s)
                .chain(this::removeDeploymentsInError)
                .onItem().transform(newdeployments -> {
                    deployments.forEach(deployment -> Log.infov("[Service {0}] deployment created {1}", deployment.getService().getId(), deployment.toString()));

                    return deployments;
                })
                // 9. Check the pod is ready
                .chain(this::keepReadyDeployments)
                .chain(this::createIngress)
                .chain(this::removeDeploymentsInError)
                //10. If pod is ready, change state to DEPLOYED
                .chain(this::updateServicesStateToDeployed)
                .replaceWithVoid();
    }

    private Uni<Void> deleteDeployments(List<Deployment> deployments) {
        // 2. Update services state to DELETION_IN_PROGRESS
        return this.updateServiceStateToDeletionInProgress(deployments)
                // 3. Delete service from kubernetes
                .chain(this::deleteFromK8s)
                .chain(this::removeDeploymentsInError)
                // 4. Update ingress
                .chain(this::deleteIngress)
                .chain(this::removeDeploymentsInError)
                // 5. Delete remote database user and database
                .chain(this::cleanDatabase)
                .chain(this::removeDeploymentsInError)
                // 6. Delete service
                .chain(this::cleanUpServices)
                .chain(this::removeDeploymentsInError)
                .onItem().transform(deploymentsDeleted -> {
                    deploymentsDeleted.forEach(deployment -> Log.infov("[Service {0}] deployment deleted {1}", deployment.getService().getId(), deployment.toString()));

                    return deploymentsDeleted;
                })
                .replaceWithVoid();
    }

    private Uni<Void> deployServices() {
        // 1. Retrieve services that need a deployment
        return serviceRepository.listServicesByState(State.SCHEDULED)
                // 2. Create and persist a Deployment object and update service state to IN_PROGRESS
                .chain(services -> this.createDeploymentEntitiesAndUpdateServiceState(services, State.IN_PROGRESS))
                // 3. In case of Prestashop, create admin credentials
                .chain(this::createPrestashopAdminCredentials)
                // 4. In case of WordPress, create admin credentials
                .chain(this::createWordpressAdminCredentials)
                // 5. FInd the Deployment database and persist it
                .chain(this::findDeploymentDatabase)
                // 6. Based on the Deployment database configuration, create the remote user with password, create the database
                .chain(this::createRemoteDatabase)
                .chain(this::removeDeploymentsInError)
                // 7. Create CRD and send CRD to Kubernetes cluster
                .chain(this::deployToK8s)
                .chain(this::removeDeploymentsInError)
                // 8. log
                .onItem().transform(deployments -> {
                    deployments.forEach(deployment -> Log.infov("[Service {0}] deployment created {1}", deployment.getService().getId(), deployment.toString()));

                    return deployments;
                })
                // 9. Check the pod is ready
                .chain(this::keepReadyDeployments)
                .chain(this::createIngress)
                .chain(this::removeDeploymentsInError)
                //10. If pod is ready, change state to DEPLOYED
                .chain(this::updateServicesStateToDeployed)
                .replaceWithVoid();
    }

    private Uni<List<Deployment>> createPrestashopAdminCredentials(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments.stream().map(deployment -> {
                    if (!Type.PRESTASHOP.equals(deployment.getService().getType())) {
                        return Uni.createFrom().item(deployment);
                    }

                    return Panache.withTransaction(() -> {
                                final String adminUser = String.format("%s@%s", deployment.getService().getId(), clusterConfiguration.getPsEmailHostname());
                                final String adminPwd = UserUtils.generatePassword();
                                deployment.getService().setAdminUser(adminUser);
                                deployment.getService().setAdminPassword(adminPwd);
                                deployment.getService().setOwnerAdminUser(adminUser);
                                deployment.getService().setOwnerAdminPassword(adminPwd);

                                return serviceRepository.persist(deployment.getService()).replaceWith(deployment);
                            })
                            .onFailure().recoverWithUni(throwable -> setDeploymentError(deployment, throwable.getMessage(), State.DELETION_IN_ERROR));
                }).toList())
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .collect().asList();

    }

    private Uni<List<Deployment>> createWordpressAdminCredentials(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments.stream().map(deployment -> {
                    if (!Type.WORDPRESS.equals(deployment.getService().getType())) {
                        return Uni.createFrom().item(deployment);
                    }

                    return Panache.withTransaction(() -> {
                                final String adminUser = String.format("%s@%s", deployment.getService().getId(), clusterConfiguration.getWpEmailHostname());
                                final String adminPwd = UserUtils.generatePassword();
                                deployment.getService().setAdminUser(adminUser);
                                deployment.getService().setAdminPassword(adminPwd);
                                deployment.getService().setOwnerAdminUser(adminUser);
                                deployment.getService().setOwnerAdminPassword(adminPwd);

                                return serviceRepository.persist(deployment.getService()).replaceWith(deployment);
                            })
                            .onFailure().recoverWithUni(throwable -> setDeploymentError(deployment, throwable.getMessage(), State.DELETION_IN_ERROR));
                }).toList())
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .collect().asList();

    }

    private Uni<List<Deployment>> deactivateServices(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments.stream().map(deployment -> databaseFactory.getDatabaseService(deployment.getService()).deactivateService(deployment)
                        .onFailure().recoverWithUni(throwable -> setDeploymentError(deployment, throwable.getMessage(), State.DEACTIVATION_IN_ERROR))).toList())
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .collect().asList();
    }

    private Uni<List<Deployment>> reactivateServices(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments.stream().map(deployment -> databaseFactory.getDatabaseService(deployment.getService()).reactivateService(deployment)
                        .onFailure().recoverWithUni(throwable -> setDeploymentError(deployment, throwable.getMessage(), State.DEACTIVATION_IN_ERROR))).toList())
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .collect().asList();
    }

    private Uni<List<Deployment>> removeDeploymentsInError(List<Deployment> deployments) {
        return Uni.createFrom().item(deployments.stream().filter(Deployment::isNotInError).toList());
    }

    private Uni<List<Deployment>> cleanDatabase(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments.stream().map(deployment -> databaseFactory.getDatabaseService(deployment.getService()).dropDatabaseAndDatabaseUser(deployment)
                        .onFailure().recoverWithUni(throwable -> setDeploymentError(deployment, throwable.getMessage(), State.DELETION_IN_ERROR))).toList())
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .collect().asList();
    }

    private Uni<List<Service>> cleanUpDeployments(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments.stream().map(this::deleteServiceDeployment).toList())
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .collect().asList();
    }

    private Uni<List<Deployment>> cleanUpServices(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments.stream().map(deployment -> this.cleanUpService(deployment)
                        .onFailure().recoverWithUni(throwable -> setDeploymentError(deployment, throwable.getMessage(), State.DELETION_IN_ERROR))).toList())
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .collect().asList();
    }

    private Uni<Long> deleteDeployment(Deployment deployment) {
        return deploymentRepository.delete("id = ?1", deployment.getId());
    }

    private Uni<Service> deleteServiceDeployment(Deployment deployment) {
        return Panache.withTransaction(() -> deleteDeployment(deployment).replaceWith(deployment.getService()));
    }

    private Uni<Deployment> cleanUpService(Deployment deployment) {
        return Panache.withTransaction(
                () -> {
                    Log.infov("Deleting service {0}", deployment.getService().getServiceName());
                    return deleteDeployment(deployment)
                            .onItem()
                            .transformToUni(unused -> serviceRepository.delete("id = ?1", deployment.getService().getId()))
                            .replaceWith(deployment);
                });
    }

    private Uni<List<Deployment>> updateServiceStateToDeletionInProgress(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments)
                .onItem().transformToUni(deployment -> updateServiceState(deployment, State.DELETION_IN_PROGRESS))
                .concatenate().collect().asList();
    }

    private Uni<List<Deployment>> updateServiceStateToReactivationInProgress(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments)
                .onItem().transformToUni(deployment -> updateServiceState(deployment, State.REACTIVATION_IN_PROGRESS))
                .concatenate().collect().asList();
    }

    private Uni<List<Deployment>> updateServiceStateToDeactivationInProgress(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments)
                .onItem().transformToUni(deployment -> updateServiceState(deployment, State.DEACTIVATION_IN_PROGRESS))
                .concatenate().collect().asList();
    }

    private Uni<List<Deployment>> updateServiceStateToDisabled(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments)
                .onItem().transformToUni(deployment -> updateServiceState(deployment, State.DISABLED))
                .concatenate().collect().asList();
    }

    private Uni<List<Deployment>> updateServiceStateToResetInProgress(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments)
                .onItem().transformToUni(deployment -> updateServiceState(deployment, State.RESET_IN_PROGRESS))
                .concatenate().collect().asList();
    }

    private Uni<List<Deployment>> deleteIngress(List<Deployment> deployments) {
        if (deployments.isEmpty()) {
            return Uni.createFrom().item(deployments);
        }

        Log.info("Deleting Ingress");

        return Multi.createFrom().iterable(deployments)
                .onItem().transformToUniAndConcatenate(deployment -> {
                    return vertx.executeBlocking(() -> {
                                Log.infov(
                                        "Deleting ingress {0}-{1}",
                                        deployment.getService().getType().getValue().toLowerCase(),
                                        deployment.getService().getId()
                                );

                                return k8sClient.network()
                                        .v1()
                                        .ingresses()
                                        .inNamespace(clusterConfiguration.getK8sNamespace())
                                        .withName(
                                                "%s-%s".formatted(
                                                        deployment.getService().getType().getValue().toLowerCase(),
                                                        deployment.getService().getId()
                                                )
                                        )
                                        .delete();
                            })
                            .replaceWith(deployment)
                            .onFailure().recoverWithUni(t ->
                                    setDeploymentError(
                                            deployment,
                                            t.getMessage(),
                                            State.DELETION_IN_ERROR
                                    )
                            );
                })
                .collect().asList();
    }


    private Uni<List<Deployment>> createIngress(List<Deployment> deployments) {
        if (deployments.isEmpty()) {
            return Uni.createFrom().item(deployments);
        }

        Log.info("Creating Ingress");

        return Multi.createFrom().iterable(deployments)
                .onItem().transformToUniAndConcatenate(deployment -> {

                    var ingress = new NginxIngress(
                            List.of(deployment),
                            "%s-%s".formatted(
                                    deployment.getService().getType().getValue().toLowerCase(),
                                    deployment.getService().getId()
                            )
                    ).setPublicHostName(clusterConfiguration.getServicePublicHostname())
                     .setTlsSecret(clusterConfiguration.getTlsSecretName());

                    Log.infov(
                            "Creating ingress {0}-{1}",
                            deployment.getService().getType().getValue().toLowerCase(),
                            deployment.getService().getId()
                    );

                    return vertx.executeBlocking(() ->
                                    k8sClient.network()
                                            .v1()
                                            .ingresses()
                                            .inNamespace(clusterConfiguration.getK8sNamespace())
                                            .resource(ingress.get())
                                            .createOr(NonDeletingOperation::update)
                            )
                            .replaceWith(deployment)
                            .onFailure().recoverWithUni(t ->
                                    setDeploymentError(
                                            deployment,
                                            t.getMessage(),
                                            State.DEPLOYMENT_IN_ERROR
                                    )
                            );
                })
                .collect().asList();
    }

    private Uni<Boolean> isDeploymentReady(Deployment deployment) {
        Uni<Boolean> podsReady = vertx.executeBlocking(() -> {
            var pods = k8sClient.pods()
                    .inNamespace(clusterConfiguration.getK8sNamespace())
                    .withLabel("app", deployment.getService().getServiceName())
                    .list()
                    .getItems();

            return pods.stream()
                    .filter(this::isServiceType)
                    .allMatch(this::isServiceReady);
        });

        Uni<Boolean> siteReady = databaseFactory.getDatabaseService(deployment.getService()).isInstalled(deployment);

        return Uni.combine().all().unis(podsReady, siteReady)
                .combinedWith(results -> {
                    boolean podsOk = (Boolean) results.get(0);
                    boolean sitesOk = (Boolean) results.get(1);
                    Log.infov("Pod and Site are ready for service {0} ? : {1}", deployment.getService().getServiceName(), podsOk && sitesOk);
                    return podsOk && sitesOk;
                });
    }

    private Uni<List<Deployment>> keepReadyDeployments(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments)
                .onItem().transformToUniAndConcatenate(deployment ->
                        isDeploymentReady(deployment)
                                .onItem().transform(isReady -> isReady ? deployment : null)
                )
                .filter(d -> d != null)
                .collect().asList();
    }

    private boolean isServiceType(Pod pod) {
        return PRESTASHOP.equals(pod.getSpec().getContainers().getFirst().getName()) ||
                WORDPRESS.equals(pod.getSpec().getContainers().getFirst().getName());
    }

    private boolean isServiceReady(Pod resource) {
        // Equivalent of doing this check resource.getStatus().getContainerStatuses().getFirst().getReady();
        // to avoid java.util.NoSuchElementException
        return Optional.ofNullable(resource.getStatus())
                .map(PodStatus::getContainerStatuses)
                .filter(statuses -> !statuses.isEmpty())
                .map(statuses -> statuses.getFirst().getReady())
                .orElse(false);
    }

    private Uni<List<Deployment>> deleteFromK8s(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments.stream().map(deployment -> {
                    var dbSecret = new DatabaseSecret(deployment.getService().getId(), deployment.getDbPassword());
                    var cr = factory.get(deployment).setDbSecret(dbSecret).getManifest();

                    var delete = vertx.executeBlocking(() -> {
                        k8sClient.resource(Serialization.asYaml(cr)).inNamespace(clusterConfiguration.getK8sNamespace()).delete();
                        return k8sClient.resource(Serialization.asYaml(dbSecret.getSecret())).inNamespace(clusterConfiguration.getK8sNamespace()).delete();
                    });

                    return delete.onItem().transform(unused -> deployment)
                            .onFailure().recoverWithUni(throwable -> setDeploymentError(deployment, throwable.getMessage(), State.DELETION_IN_ERROR));
                }).toList())
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .collect().asList();
    }

    private Uni<List<Deployment>> deployToK8s(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments.stream().map(deployment -> {
                    var dbSecret = new DatabaseSecret(deployment.getService().getId(), deployment.getDbPassword());
                    var cr = factory.get(deployment).setDbSecret(dbSecret).getManifest();

                    Log.infof("deployment by unit : %s", deployment.toString());
                    var apply = vertx.executeBlocking(() -> {
                        k8sClient.resource(Serialization.asYaml(dbSecret.getSecret())).inNamespace(clusterConfiguration.getK8sNamespace()).createOr(NonDeletingOperation::update);
                        return k8sClient.resource(Serialization.asYaml(cr)).inNamespace(clusterConfiguration.getK8sNamespace()).createOr(NonDeletingOperation::update);
                    });

                    return apply.onItem().transform(unused -> deployment).onFailure()
                            .recoverWithUni(throwable -> setDeploymentError(deployment, throwable.getMessage(), State.DEPLOYMENT_IN_ERROR));
                }).toList())
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .collect().asList();
    }

    private Uni<Deployment> setDeploymentError(Deployment deployment, String error, State errorState) {
        Log.errorv(
                "ERROR {0}, {1}-{2} : {3}", errorState.getValue(),
                deployment.getService().getType().getValue(),
                deployment.getService().getId(), error
        );
        return deploymentRepository.setError(deployment, error).onItem().transformToUni(dep -> updateServiceState(dep, errorState));
    }

    private Uni<List<Deployment>> findDeploymentDatabase(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments)
                .onItem().transformToUniAndConcatenate(deployment -> databaseRepository.getRandomDatabase()
                        .onItem().transform(database -> deploymentRepository.persist(deployment.setDatabase(database))))
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .collect().asList();
    }

    private Uni<List<Deployment>> createDeploymentEntitiesAndUpdateServiceState(List<Service> services, State state) {
        return Multi.createFrom().iterable(services.stream().map(this::createDeploymentEntity).toList())
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .onItem().transformToUni(deployment -> this.updateServiceState(deployment, state))
                .concatenate().collect().asList();
    }

    private Uni<Deployment> createDeploymentEntity(Service service) {
        var deployment = new Deployment()
                .setService(service)
                .setPublicHostname(clusterConfiguration.getServicePublicHostname())
                .setPath(Deployment.generatePath(service));

        return deploymentRepository.persist(deployment);
    }

    private Uni<Deployment> updateServiceState(Deployment deployment, State state) {
        return serviceRepository.updateState(deployment.getService(), state)
                .replaceWith(Uni.createFrom().item(deployment));
    }

    private Uni<List<Deployment>> updateServicesStateToDeployed(List<Deployment> deployments) {
        return Multi.createFrom().iterable(deployments)
                .onItem().transformToUniAndConcatenate(deployment -> this.updateServiceState(deployment, State.DEPLOYED))
                .collect().asList()
                .onItem().transform(deps -> {
                    if (!deps.isEmpty()) Log.infov("{0} services deployed", deployments.size());
                    return deps;
                });
    }

    private Uni<List<Deployment>> createRemoteDatabase(List<Deployment> deployments) {
        var unis = deployments.stream().map(deployment -> databaseFactory.getDatabaseService(deployment.getService()).createDatabaseAndGenerateAuthentication(deployment)
                        .onFailure().recoverWithUni(throwable -> setDeploymentError(deployment, throwable.getMessage(), State.DEPLOYMENT_IN_ERROR)))
                .toList();

        return Multi.createFrom().iterable(unis)
                .onItem().transformToMultiAndConcatenate(Uni::toMulti)
                .collect().asList();
    }

}
