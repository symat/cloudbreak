package com.sequenceiq.it.cloudbreak.testcase.e2e.l0promotion;

import static com.sequenceiq.cloudbreak.api.endpoint.v4.recipes.requests.RecipeV4Type.POST_CLOUDERA_MANAGER_START;
import static com.sequenceiq.it.cloudbreak.cloud.HostGroupType.IDBROKER;
import static com.sequenceiq.it.cloudbreak.cloud.HostGroupType.MASTER;
import static com.sequenceiq.it.cloudbreak.context.RunningParameter.key;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.sequenceiq.freeipa.api.v1.operation.model.OperationState;
import com.sequenceiq.it.cloudbreak.action.ums.SetWorkloadPasswordAction;
import com.sequenceiq.it.cloudbreak.client.FreeIpaTestClient;
import com.sequenceiq.it.cloudbreak.client.RecipeTestClient;
import com.sequenceiq.it.cloudbreak.client.SdxTestClient;
import com.sequenceiq.it.cloudbreak.client.UmsTestClient;
import com.sequenceiq.it.cloudbreak.context.Description;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.ClouderaManagerTestDto;
import com.sequenceiq.it.cloudbreak.dto.ClusterTestDto;
import com.sequenceiq.it.cloudbreak.dto.InstanceGroupTestDto;
import com.sequenceiq.it.cloudbreak.dto.environment.EnvironmentTestDto;
import com.sequenceiq.it.cloudbreak.dto.freeipa.FreeIpaTestDto;
import com.sequenceiq.it.cloudbreak.dto.freeipa.FreeIpaUserSyncTestDto;
import com.sequenceiq.it.cloudbreak.dto.recipe.RecipeTestDto;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxInternalTestDto;
import com.sequenceiq.it.cloudbreak.dto.stack.StackTestDto;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsTestDto;
import com.sequenceiq.it.cloudbreak.testcase.e2e.sdx.PreconditionSdxE2ETest;
import com.sequenceiq.it.cloudbreak.util.clouderamanager.ClouderaManagerUtil;
import com.sequenceiq.it.cloudbreak.util.ssh.SshJUtil;
import com.sequenceiq.it.util.ResourceUtil;
import com.sequenceiq.sdx.api.model.SdxClusterStatusResponse;

public class InternalSdxSshAndCmAccessTest extends PreconditionSdxE2ETest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetWorkloadPasswordAction.class);

    @Inject
    private SdxTestClient sdxTestClient;

    @Inject
    private RecipeTestClient recipeTestClient;

    @Inject
    private FreeIpaTestClient freeIpaTestClient;

    @Inject
    private UmsTestClient umsTestClient;

    @Inject
    private SshJUtil sshJUtil;

    @Inject
    private ClouderaManagerUtil clouderaManagerUtil;

    @Test(dataProvider = TEST_CONTEXT)
    @Description(
            given = "there is a running Manowar SDX cluster in available state",
            when = "SSH to the SDX nodes with 'admin' then acting user with new workload password, where the 'post-install' recipe had been installed.",
            and = "log in to the Cloudera Manager, where 'knox-IDBROKER-BASE' role config mappings then related Cloudera Manager user details have been read",
            then = "SSH and Cloudera Manager API access should be successful, 'post-install' recipe files and requested Cloudera Manager details should be" +
                    " present"
    )
    public void testSdxSshAndCmAccessWithNewWorkloadPassword(TestContext testContext) throws IOException {
        String freeIpa = testContext.given(FreeIpaTestDto.class).when(freeIpaTestClient.describe()).getResponse().getName();
        String sdxInternal = resourcePropertyProvider().getName();
        String cluster = resourcePropertyProvider().getName();
        String clouderaManager = resourcePropertyProvider().getName();
        String recipeName = resourcePropertyProvider().getName();
        String stack = resourcePropertyProvider().getName();
        String filePath = "/post-install";
        String fileName = "post-install";
        String masterInstanceGroup = "master";
        String idbrokerInstanceGroup = "idbroker";
        String newWorkloadPassword = "Admin123@";
        String workloadUsername = getWorkloadUsername(testContext);

        testContext
                .given(clouderaManager, ClouderaManagerTestDto.class)
                .given(cluster, ClusterTestDto.class).withBlueprintName(getDefaultSDXBlueprintName()).withValidateBlueprint(Boolean.FALSE)
                .withClouderaManager(clouderaManager)
                .given(RecipeTestDto.class).withName(recipeName).withContent(generateRecipeContent())
                .withRecipeType(POST_CLOUDERA_MANAGER_START)
                .when(recipeTestClient.createV4())
                .given(masterInstanceGroup, InstanceGroupTestDto.class).withHostGroup(MASTER).withNodeCount(1).withRecipes(recipeName)
                .given(idbrokerInstanceGroup, InstanceGroupTestDto.class).withHostGroup(IDBROKER).withNodeCount(1).withRecipes(recipeName)
                .given(stack, StackTestDto.class).withCluster(cluster).withInstanceGroups(masterInstanceGroup, idbrokerInstanceGroup)
                .given(sdxInternal, SdxInternalTestDto.class)
                .withCloudStorage(getCloudStorageRequest(testContext))
                .withStackRequest(key(cluster), key(stack))
                .when(sdxTestClient.createInternal(), key(sdxInternal))
                .await(SdxClusterStatusResponse.RUNNING)
                .awaitForInstance(getSdxInstancesHealthyState())
                .then((tc, testDto, client) -> sshJUtil.checkFilesOnHostByNameAndPath(testDto, client, List.of(MASTER.getName(), IDBROKER.getName()),
                            filePath, fileName, 1, null, null))
                .then((tc, testDto, client) -> clouderaManagerUtil.checkClouderaManagerKnoxIDBrokerRoleConfigGroups(tc, testDto, client))
                .validate();

        testContext
                .given(freeIpa, FreeIpaTestDto.class)
                .when(freeIpaTestClient.describe(), key(freeIpa))
                .given(FreeIpaUserSyncTestDto.class)
                .when(freeIpaTestClient.getLastSyncOperationStatus())
                .await(OperationState.COMPLETED)
                .given(UmsTestDto.class).assignTarget(EnvironmentTestDto.class.getSimpleName())
                .when(umsTestClient.setWorkloadPassword(newWorkloadPassword))
                .given(FreeIpaUserSyncTestDto.class)
                .when(freeIpaTestClient.syncAll())
                .await(OperationState.COMPLETED)
                .given(sdxInternal, SdxInternalTestDto.class)
                .then((tc, testDto, client) -> sshJUtil.checkFilesOnHostByNameAndPath(testDto, client, List.of(MASTER.getName()),
                            filePath, fileName, 1, workloadUsername, newWorkloadPassword))
                .then((tc, testDto, client) -> clouderaManagerUtil.checkClouderaManagerUserDetails(tc, testDto, client, workloadUsername, newWorkloadPassword))
                .validate();
    }

    private String generateRecipeContent() throws IOException {
        String recipeContentFromFile = ResourceUtil.readResourceAsString(applicationContext, getRecipePath());
        return Base64.encodeBase64String(recipeContentFromFile.getBytes());
    }

    private String getWorkloadUsername(TestContext testContext) {
        AtomicReference<String> workloadUsername = new AtomicReference<>("csso_cloudbreak-qe");
        testContext.given(UmsTestDto.class).assignTarget(EnvironmentTestDto.class.getSimpleName())
                .when(umsTestClient.getUserDetails())
                .then((tc, testDto, client) ->  {
                    String userCrn = tc.getActingUserCrn().toString();
                    workloadUsername.set(client.getDefaultClient().getUserDetails(userCrn, userCrn, Optional.of("")).getWorkloadUsername());
                    LOGGER.info("Found workload username: '{}' for test: '{}'", workloadUsername.get(), getClass().getSimpleName());
                    return testDto;
                });
        return workloadUsername.get();
    }
}
