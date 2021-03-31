package com.sequenceiq.it.cloudbreak.util.clouderamanager;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sequenceiq.it.cloudbreak.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.SdxClient;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.distrox.DistroXTestDto;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxInternalTestDto;
import com.sequenceiq.it.cloudbreak.dto.stack.StackTestDto;
import com.sequenceiq.it.cloudbreak.util.clouderamanager.action.ClouderaManagerClientActions;

@Component
public class ClouderaManagerUtil {

    @Inject
    private ClouderaManagerClientActions clouderaManagerClientActions;

    private ClouderaManagerUtil() {
    }

    public StackTestDto checkClouderaManagerUser(TestContext testContext, StackTestDto stackTestDto, CloudbreakClient cloudbreakClient) {
        return clouderaManagerClientActions.checkCmTestUser(testContext, stackTestDto, cloudbreakClient);
    }

    public SdxInternalTestDto checkClouderaManagerKnoxIDBrokerRoleConfigGroups(TestContext testContext, SdxInternalTestDto sdxInternalTestDto,
            SdxClient sdxClient) {
        return clouderaManagerClientActions.checkCmKnoxIDBrokerRoleConfigGroups(testContext, sdxInternalTestDto, sdxClient);
    }

    public DistroXTestDto checkClouderaManagerUsers(TestContext testContext, DistroXTestDto distroXTestDto, CloudbreakClient cloudbreakClient) {
        return clouderaManagerClientActions.checkClouderaManagerUsers(testContext, distroXTestDto, cloudbreakClient);
    }

    public SdxInternalTestDto checkClouderaManagerUserDetails(TestContext testContext, SdxInternalTestDto sdxInternalTestDto, SdxClient sdxClient,
            String user, String password) {
        return clouderaManagerClientActions.checkClouderaManagerUserDetails(testContext, sdxInternalTestDto, sdxClient, user, password);
    }
}
