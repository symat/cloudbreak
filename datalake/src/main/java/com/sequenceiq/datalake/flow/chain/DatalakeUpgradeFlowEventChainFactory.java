package com.sequenceiq.datalake.flow.chain;

import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.datalake.flow.datalake.upgrade.event.DatalakeUpgradeFlowChainStartEvent;
import com.sequenceiq.datalake.flow.datalake.upgrade.event.DatalakeUpgradeStartEvent;
import com.sequenceiq.datalake.flow.dr.backup.event.DatalakeTrigggerBackupEvent;
import com.sequenceiq.flow.core.chain.FlowEventChainFactory;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.sequenceiq.datalake.flow.datalake.upgrade.DatalakeUpgradeEvent.DATALAKE_UPGRADE_EVENT;
import static com.sequenceiq.datalake.flow.datalake.upgrade.DatalakeUpgradeEvent.DATALAKE_UPGRADE_FLOW_CHAIN_EVENT;
import static com.sequenceiq.datalake.flow.dr.backup.DatalakeBackupEvent.DATALAKE_TRIGGER_BACKUP_EVENT;

@Component
public class DatalakeUpgradeFlowEventChainFactory implements FlowEventChainFactory<DatalakeUpgradeFlowChainStartEvent> {
    @Override
    public String initEvent() {
        return DATALAKE_UPGRADE_FLOW_CHAIN_EVENT.event();
    }

    @Override
    public Queue<Selectable> createFlowTriggerEventQueue(DatalakeUpgradeFlowChainStartEvent event) {
        Queue<Selectable> chain = new ConcurrentLinkedQueue<>();
        chain.add(new DatalakeTrigggerBackupEvent(DATALAKE_TRIGGER_BACKUP_EVENT.event(),
                event.getResourceId(), event.getUserId(), event.getBackupLocation(), ""));
        chain.add(new DatalakeUpgradeStartEvent(DATALAKE_UPGRADE_EVENT.event(), event.getResourceId(), event.getUserId(),
                event.getImageId(), event.getReplaceVms()));
        return chain;
    }
}
