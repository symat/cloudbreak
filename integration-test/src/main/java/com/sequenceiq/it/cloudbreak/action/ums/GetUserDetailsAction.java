package com.sequenceiq.it.cloudbreak.action.ums;

import static java.lang.String.format;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto;
import com.sequenceiq.it.cloudbreak.UmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsTestDto;
import com.sequenceiq.it.cloudbreak.log.Log;

public class GetUserDetailsAction implements Action<UmsTestDto, UmsClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetUserDetailsAction.class);

    @Override
    public UmsTestDto action(TestContext testContext, UmsTestDto testDto, UmsClient client) throws Exception {
        String userCrn = testContext.getActingUserCrn().toString();
        LOGGER.info("Getting user '{}' details.", userCrn);
        Log.when(LOGGER, format(" Getting user '%s' details. ", userCrn));
        UserManagementProto.User user = client.getDefaultClient().getUserDetails(userCrn, userCrn, Optional.of(""));
        // wait for UmsRightsCache to expire
        LOGGER.info("User details \ncrn: {} \nworkload username: {} \nfirst name: {} \nlast name: {} \nstate: {} \ncreation date: {} \nemail: {} " +
                        "\nexternal user id: {} \nSFDC contact id: {}", user.getCrn(), user.getWorkloadUsername(), user.getFirstName(), user.getLastName(),
                user.getState().name(), user.getCreationDate(), user.getEmail(), user.getExternalUserId(), user.getSfdcContactId());
        Log.when(LOGGER, format(" User details %ncrn: %s %nworkload username: %s %nfirst name: %s %nlast name: %s %nstate: %s %ncreation date: %s " +
                        "%nemail: %s %nexternal user id: %s %nSFDC contact id: %s ", user.getCrn(), user.getWorkloadUsername(), user.getFirstName(),
                user.getLastName(), user.getState().name(), user.getCreationDate(), user.getEmail(), user.getExternalUserId(), user.getSfdcContactId()));
        return testDto;
    }
}
