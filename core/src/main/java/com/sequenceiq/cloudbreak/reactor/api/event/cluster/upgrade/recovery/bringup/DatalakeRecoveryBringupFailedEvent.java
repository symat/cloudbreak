package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.bringup;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.DetailedStackStatus;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;

public class DatalakeRecoveryBringupFailedEvent extends StackEvent {

    private final Exception exception;

    private final DetailedStackStatus detailedStatus;

    public DatalakeRecoveryBringupFailedEvent(Long stackId, Exception exception, DetailedStackStatus detailedStatus) {
        super(stackId);
        this.exception = exception;
        this.detailedStatus = detailedStatus;
    }

    public static DatalakeRecoveryBringupFailedEvent from(StackEvent event, Exception exception, DetailedStackStatus detailedStatus) {
        return new DatalakeRecoveryBringupFailedEvent(event.getResourceId(), exception, detailedStatus);
    }

    public Exception getException() {
        return exception;
    }

    public DetailedStackStatus getDetailedStatus() {
        return detailedStatus;
    }
}
