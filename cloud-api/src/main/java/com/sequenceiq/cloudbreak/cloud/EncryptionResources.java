package com.sequenceiq.cloudbreak.cloud;

import com.sequenceiq.cloudbreak.cloud.model.encryption.CreatedEncryptionResources;
import com.sequenceiq.cloudbreak.cloud.model.encryption.EncryptionResourcesCreationRequest;
import com.sequenceiq.cloudbreak.cloud.model.encryption.EncryptionResourcesDeletionRequest;

public interface EncryptionResources extends CloudPlatformAware {
    CreatedEncryptionResources createDiskEncryptionSet(EncryptionResourcesCreationRequest encryptionResourcesCreationRequest);

    void deleteDiskEncryptionSet(EncryptionResourcesDeletionRequest encryptionResourcesDeletionRequest);
}