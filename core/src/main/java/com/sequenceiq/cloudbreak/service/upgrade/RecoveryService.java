package com.sequenceiq.cloudbreak.service.upgrade;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.dto.NameOrCrn;
import com.sequenceiq.cloudbreak.core.flow2.service.ReactorFlowManager;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.logger.MDCBuilder;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.flow.api.model.FlowIdentifier;

@Component
public class RecoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryService.class);

    @Inject
    private StackService stackService;

    @Inject
    private ReactorFlowManager flowManager;

    public FlowIdentifier recoverFailedUpgrade(Long workspaceId, NameOrCrn stackNameOrCrn) {
        Stack stack = stackService.getByNameOrCrnInWorkspace(stackNameOrCrn, workspaceId);
        MDCBuilder.buildMdcContext(stack);
        return flowManager.triggerDatalakeClusterRecovery(stack.getId());
    }
}
