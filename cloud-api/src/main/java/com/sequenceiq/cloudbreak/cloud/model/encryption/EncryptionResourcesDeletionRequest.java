package com.sequenceiq.cloudbreak.cloud.model.encryption;

import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;

public class EncryptionResourcesDeletionRequest {
    private final String resourceGroupName;

    private final String diskEncryptionSetName;

    private final CloudCredential cloudCredential;

    private final boolean singleResourceGroup;

    private EncryptionResourcesDeletionRequest(Builder builder) {
        this.cloudCredential = builder.cloudCredential;
        this.resourceGroupName = builder.resourceGroupName;
        this.diskEncryptionSetName = builder.diskEncryptionSetName;
        this.singleResourceGroup = builder.singleResourceGroup;
    }

    public CloudCredential getCloudCredential() {
        return cloudCredential;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public String getDiskEncryptionSetName() {
        return diskEncryptionSetName;
    }

    public boolean isSingleResourceGroup() {
        return singleResourceGroup;
    }

    public static final class Builder {

        private CloudCredential cloudCredential;

        private String diskEncryptionSetName;

        private String resourceGroupName;

        private boolean singleResourceGroup;

        public Builder() {
        }

        public Builder withCloudCredential(CloudCredential cloudCredential) {
            this.cloudCredential = cloudCredential;
            return this;
        }

        public Builder withDiskEncryptionSetName(String diskEncryptionSetName) {
            this.diskEncryptionSetName = diskEncryptionSetName;
            return this;
        }

        public Builder withResourceGroupName(String resourceGroupName) {
            this.resourceGroupName = resourceGroupName;
            return this;
        }

        public Builder withSingleResourceGroup(boolean singleResourceGroup) {
            this.singleResourceGroup = singleResourceGroup;
            return this;
        }

        public EncryptionResourcesDeletionRequest build() {
            return new EncryptionResourcesDeletionRequest(this);
        }
    }
}
