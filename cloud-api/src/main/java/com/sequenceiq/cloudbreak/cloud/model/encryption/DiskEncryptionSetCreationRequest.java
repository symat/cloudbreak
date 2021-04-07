package com.sequenceiq.cloudbreak.cloud.model.encryption;

import java.util.HashMap;
import java.util.Map;

import com.sequenceiq.cloudbreak.cloud.CloudPlatformAware;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.Region;
import com.sequenceiq.cloudbreak.cloud.model.Variant;

public class DiskEncryptionSetCreationRequest implements CloudPlatformAware {

    private final String cloudPlatform;

    private final CloudCredential cloudCredential;

    private final Region region;

    private final String resourceGroupName;

    private final boolean singleResourceGroup;

    private final Long environmentId;

    private final String environmentName;

    private final Map<String, String> tags;

    private final String encryptionKeyUrl;

    private DiskEncryptionSetCreationRequest(Builder builder) {
        this.cloudPlatform = builder.cloudPlatform;
        this.cloudCredential = builder.cloudCredential;
        this.region = builder.region;
        this.resourceGroupName = builder.resourceGroupName;
        this.singleResourceGroup = builder.singleResourceGroup;
        this.environmentId = builder.environmentId;
        this.environmentName = builder.environmentName;
        this.tags = builder.tags;
        this.encryptionKeyUrl = builder.encryptionKeyUrl;
    }

    public String getCloudPlatform() {
        return cloudPlatform;
    }

    public CloudCredential getCloudCredential() {
        return cloudCredential;
    }

    public Region getRegion() {
        return region;
    }

    public String getResourceGroup() {
        return resourceGroupName;
    }

    public boolean isSingleResourceGroup() {
        return singleResourceGroup;
    }

    public Long getEnvironmentId() {
        return environmentId;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getEncryptionKeyUrl() {
        return encryptionKeyUrl;
    }

    @Override
    public Platform platform() {
        return Platform.platform(cloudPlatform);
    }

    @Override
    public Variant variant() {
        return Variant.variant(cloudPlatform);
    }

    public static final class Builder {

        private String cloudPlatform;

        private CloudCredential cloudCredential;

        private Region region;

        private String resourceGroupName;

        private boolean singleResourceGroup;

        private Long environmentId;

        private String environmentName;

        private Map<String, String> tags = new HashMap<>();

        private String encryptionKeyUrl;

        public Builder() {
        }

        public Builder withCloudPlatform(String cloudPlatform) {
            this.cloudPlatform = cloudPlatform;
            return this;
        }

        public Builder withEnvironmentId(Long environmentId) {
            this.environmentId = environmentId;
            return this;
        }

        public Builder withEnvironmentName(String environmentName) {
            this.environmentName = environmentName;
            return this;
        }

        public Builder withCloudCredential(CloudCredential cloudCredential) {
            this.cloudCredential = cloudCredential;
            return this;
        }

        public Builder withRegion(Region region) {
            this.region = region;
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

        public Builder withTags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder withEncryptionKeyUrl(String encryptionKeyUrl) {
            this.encryptionKeyUrl = encryptionKeyUrl;
            return this;
        }

        public DiskEncryptionSetCreationRequest build() {
            return new DiskEncryptionSetCreationRequest(this);
        }
    }
}