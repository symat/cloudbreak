package com.sequenceiq.it.cloudbreak.util.clouderamanager.action;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cloudera.api.swagger.RoleConfigGroupsResourceApi;
import com.cloudera.api.swagger.UsersResourceApi;
import com.cloudera.api.swagger.client.ApiClient;
import com.cloudera.api.swagger.client.ApiException;
import com.cloudera.api.swagger.model.ApiAuthRoleRef;
import com.cloudera.api.swagger.model.ApiConfigList;
import com.cloudera.api.swagger.model.ApiUser2;
import com.cloudera.api.swagger.model.ApiUser2List;
import com.google.common.base.Strings;
import com.sequenceiq.it.cloudbreak.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.SdxClient;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.distrox.DistroXTestDto;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxInternalTestDto;
import com.sequenceiq.it.cloudbreak.dto.stack.StackTestDto;
import com.sequenceiq.it.cloudbreak.exception.TestFailException;
import com.sequenceiq.it.cloudbreak.util.clouderamanager.client.ClouderaManagerClient;

@Component
public class ClouderaManagerClientActions extends ClouderaManagerClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClouderaManagerClientActions.class);

    private static final String API_ROOT = "/api";

    private static final String API_V_43 = API_ROOT + "/v43";

    @Value("${integrationtest.clouderamanager.defaultUser:}")
    private String cmUser;

    @Value("${integrationtest.clouderamanager.defaultPassword:}")
    private String cmPassword;

    @Value("${integrationtest.cloudProvider:}")
    private String cloudProvider;

    public StackTestDto checkCmTestUser(TestContext testContext, StackTestDto stackTestDto, CloudbreakClient cloudbreakClient) {
        String serverFqdn = stackTestDto.getResponse().getCluster().getServerFqdn();
        String userDetails = "";
        ApiClient apiClient = getCmApiClient(serverFqdn, API_V_43, cmUser, cmPassword);
        // CHECKSTYLE:OFF
        UsersResourceApi usersResourceApi = new UsersResourceApi(apiClient);
        // CHECKSTYLE:ON
        try {
            ApiUser2 testUserDetails = usersResourceApi.readUser2("teszt");
            String testUserName = String.valueOf(testUserDetails.getName());
            List<ApiAuthRoleRef> testUserAuthRoles = testUserDetails.getAuthRoles();
            Optional<ApiAuthRoleRef> testUserDisplayName = testUserAuthRoles.stream()
                    .filter(userAuthRole -> userAuthRole.getDisplayName().equals("Full Administrator"))
                    .findFirst();
            if (testUserDisplayName.isPresent()) {
                LOGGER.info("Test user exist with desired role: {}", testUserDisplayName);
            } else {
                LOGGER.error("Test user exist with desired role: {}", testUserDisplayName);
                throw new TestFailException("Test user exist! However with different role: " + testUserDisplayName);
            }
            userDetails = String.valueOf(testUserDetails);
            if (Strings.isNullOrEmpty(testUserName)) {
                LOGGER.error("Requested user does not exist: " + userDetails);
                throw new TestFailException("Requested user is not exist");
            } else if (!"teszt".equals(testUserName)) {
                LOGGER.error("Requested user details are not valid: {}", userDetails);
                throw new TestFailException("Requested user details are not valid " + userDetails);
            }
        } catch (Exception e) {
            LOGGER.error("Can't get users' list at: {} or test user is not valid with {}", apiClient.getBasePath(), userDetails);
            throw new TestFailException("Can't get users' list at: " + apiClient.getBasePath() + " or test user is not valid with " + userDetails, e);
        }
        return stackTestDto;
    }

    public SdxInternalTestDto checkCmKnoxIDBrokerRoleConfigGroups(TestContext testContext, SdxInternalTestDto sdxInternalTestDto, SdxClient sdxClient) {
        String serverFqdn = sdxInternalTestDto.getResponse().getStackV4Response().getCluster().getServerFqdn();
        ApiClient apiClient = getCmApiClient(serverFqdn, API_V_43, cmUser, cmPassword);
        // CHECKSTYLE:OFF
        RoleConfigGroupsResourceApi roleConfigGroupsResourceApi = new RoleConfigGroupsResourceApi(apiClient);
        // CHECKSTYLE:ON
        try {
            ApiConfigList knoxConfigs = roleConfigGroupsResourceApi.readConfig(sdxInternalTestDto.getName(), "knox-IDBROKER-BASE",
                    "knox", "full");
            knoxConfigs.getItems().stream()
                    .forEach(knoxConfig -> {
                        String knoxConfigName = knoxConfig.getName();
                        String mappingsFromKnoxConfig = knoxConfig.getValue();
                        if (String.join("_", "idbroker", cloudProvider, "group", "mapping").equalsIgnoreCase(knoxConfigName)) {
                            if (!mappingsFromKnoxConfig.contains("_c_cm_admins_")) {
                                LOGGER.error("{} does not contains the expected 'CM Admins' mapping!", knoxConfigName);
                                throw new TestFailException(String.format("%s does not contains the expected 'CM Admins' mapping!", knoxConfigName));
                            } else {
                                LOGGER.info("'{}' contains '{}' mapping.", knoxConfigName, mappingsFromKnoxConfig);
                            }
                        } else if (String.join("_", "idbroker", cloudProvider, "user", "mapping").equalsIgnoreCase(knoxConfigName)) {
                            if (!mappingsFromKnoxConfig.contains("hive")) {
                                LOGGER.error("{} does not contains the expected 'Hive' mapping!", knoxConfigName);
                                throw new TestFailException(String.format("%s does not contains the expected 'Hive' mapping!", knoxConfigName));
                            } else {
                                LOGGER.info("'{}' contains '{}' mappings.", knoxConfigName, mappingsFromKnoxConfig);
                            }
                        }
                    });
            if (knoxConfigs.getItems().isEmpty()) {
                LOGGER.error("IDBroker mappings are NOT exist!");
                throw new TestFailException("IDBroker mappings are NOT exist!");
            }
        } catch (ApiException e) {
            LOGGER.error("Exception when calling UsersResourceApi#readUsers2", e);
            throw new TestFailException("Exception when calling UsersResourceApi#readUsers2 at " + apiClient.getBasePath(), e);
        } catch (Exception e) {
            LOGGER.error("Can't get users' list at: '{}'!", apiClient.getBasePath());
            throw new TestFailException("Can't get users' list at: " + apiClient.getBasePath(), e);
        }
        return sdxInternalTestDto;
    }

    public DistroXTestDto checkClouderaManagerUsers(TestContext testContext, DistroXTestDto distroXTestDto, CloudbreakClient cloudbreakClient) {
        String serverFqdn = distroXTestDto.getResponse().getCluster().getServerFqdn();
        ApiClient apiClient = getCmApiClient(serverFqdn, API_V_43, cmUser, cmPassword);
        // CHECKSTYLE:OFF
        UsersResourceApi usersResourceApi = new UsersResourceApi(apiClient);
        // CHECKSTYLE:ON
        try {
            ApiUser2List cmUsers = usersResourceApi.readUsers2("summary");
            cmUsers.getItems().stream()
                    .forEach(apiUser2 -> {
                        String userName = String.valueOf(apiUser2.getName());
                        List<String> userAuthRoles = apiUser2.getAuthRoles().stream()
                                .map(ApiAuthRoleRef::getDisplayName)
                                .collect(Collectors.toList());
                        LOGGER.info("User '{}' is present with roles: [{}]", userName, userAuthRoles);
                    });
            if (cmUsers.getItems().isEmpty()) {
                LOGGER.error("Cloudera Manager users are NOT exist!");
                throw new TestFailException("Cloudera Manager users are NOT exist!");
            }
        } catch (ApiException e) {
            LOGGER.error("Exception when calling UsersResourceApi#readUsers2", e);
            throw new TestFailException("Exception when calling UsersResourceApi#readUsers2 at " + apiClient.getBasePath(), e);
        } catch (Exception e) {
            LOGGER.error("Can't get users' list at: '{}'!", apiClient.getBasePath());
            throw new TestFailException("Can't get users' list at: " + apiClient.getBasePath(), e);
        }
        return distroXTestDto;
    }

    public SdxInternalTestDto checkClouderaManagerUserDetails(TestContext testContext, SdxInternalTestDto sdxInternalTestDto, SdxClient sdxClient,
            String user, String password) {
        String serverFqdn = sdxInternalTestDto.getResponse().getStackV4Response().getCluster().getServerFqdn();
        ApiClient apiClient = getCmApiClient(serverFqdn, API_V_43, user, password);
        // CHECKSTYLE:OFF
        UsersResourceApi usersResourceApi = new UsersResourceApi(apiClient);
        // CHECKSTYLE:ON
        try {
            ApiUser2 cmUser = usersResourceApi.readUser2(user);
            String userName = String.valueOf(cmUser.getName());
            if (StringUtils.isBlank(userName)) {
                LOGGER.error("User '{}' is NOT exist at Cloudera Manager!", user);
                throw new TestFailException(String.format("User '%s' is NOT exist at Cloudera Manager!", user));
            }
            try {
                List<String> userAuthRoles = cmUser.getAuthRoles().stream()
                        .map(ApiAuthRoleRef::getDisplayName)
                        .collect(Collectors.toList());
                LOGGER.info("User '{}' is present with roles: [{}]", userName, userAuthRoles);
            } catch (NullPointerException e) {
                LOGGER.warn("User '{}' does not have Cloudera Manager ApiAuthRole!", userName);
            }
        } catch (ApiException e) {
            LOGGER.error("Exception when calling UsersResourceApi#readUser2", e);
            throw new TestFailException("Exception when calling UsersResourceApi#readUser2 at " + apiClient.getBasePath(), e);
        } catch (Exception e) {
            LOGGER.error("Can't get user details at: '{}'!", apiClient.getBasePath());
            throw new TestFailException("Can't get user details at: " + apiClient.getBasePath(), e);
        }
        return sdxInternalTestDto;
    }
}
