package com.sequenceiq.cloudbreak.cloud.azure;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.microsoft.azure.management.compute.implementation.DiskEncryptionSetInner;
import com.sequenceiq.cloudbreak.cloud.EncryptionResources;
import com.sequenceiq.cloudbreak.cloud.azure.client.AzureClient;
import com.sequenceiq.cloudbreak.cloud.azure.client.AzureClientService;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.Variant;
import com.sequenceiq.cloudbreak.cloud.model.encryption.CreatedDiskEncryptionSet;
import com.sequenceiq.cloudbreak.cloud.model.encryption.DiskEncryptionSetCreationRequest;

@Service
public class AzureEncryptionResources implements EncryptionResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureEncryptionResources.class);

    private static final Pattern ENCRYPTION_KEY_URL_PATTERN = Pattern.compile("https://([^.]+)\\.vault.*");

    @Inject
    private AzureClientService azureClientService;

    @Inject
    private AzureUtils azureUtils;

    @Override
    public Platform platform() {
        return AzureConstants.PLATFORM;
    }

    @Override
    public Variant variant() {
        return AzureConstants.VARIANT;
    }

    @Override
    public CreatedDiskEncryptionSet createDiskEncryptionSet(DiskEncryptionSetCreationRequest diskEncryptionSetCreationRequest) {
        AzureClient azureClient = azureClientService.getClient(diskEncryptionSetCreationRequest.getCloudCredential());
        String encryptionKeyUrl = diskEncryptionSetCreationRequest.getEncryptionKeyUrl();
        String envName = diskEncryptionSetCreationRequest.getEnvironmentName();
        String location = diskEncryptionSetCreationRequest.getRegion().getRegionName();
        Map<String, String> tags = diskEncryptionSetCreationRequest.getTags();
        String vaultName = null;
        String vaultResourceGroupName;
        String desResourceGroupName;

        Matcher matcher = ENCRYPTION_KEY_URL_PATTERN.matcher(encryptionKeyUrl);
        if (matcher.matches()) {
            vaultName = matcher.group(1);
        } else {
            throw new IllegalArgumentException("vaultName cannot be fetched from encryptionKeyUrl. encryptionKeyUrl should be of format - " +
                    "'https://<vaultName>.vault.azure.net/keys/<keyName>/<keyVersion>'");
        }
        if (diskEncryptionSetCreationRequest.isSingleResourceGroup()) {
            desResourceGroupName = diskEncryptionSetCreationRequest.getResourceGroup();
            vaultResourceGroupName = desResourceGroupName;
        } else {
            throw new IllegalArgumentException("Customer Managed Key Encryption for managed Azure disks is supported for single resource group only.");
        }
        String sourceVaultId = String.format("/subscriptions/" + azureClient.getCurrentSubscription().subscriptionId()
                + "/resourceGroups/" + vaultResourceGroupName + "/providers/Microsoft.KeyVault/vaults/"
                + vaultName);

        CreatedDiskEncryptionSet diskEncryptionSet = createDiskEncryptionSetOnCloud(azureClient, encryptionKeyUrl, envName, location,
                desResourceGroupName, tags, sourceVaultId);
        azureUtils.grantKeyVaultAccessPolicyToServicePrincipal(azureClient, vaultResourceGroupName, vaultName,
                diskEncryptionSet.getDiskEncryptionSetPrincipalId());
        return diskEncryptionSet;
    }

    public CreatedDiskEncryptionSet createDiskEncryptionSetOnCloud(AzureClient azureClient, String encryptionKeyUrl, String envName, String location,
            String desResourceGroupName, Map<String, String> tags, String sourceVaultId) {
        String diskEncryptionSetName = azureUtils.generateDesNameByNameAndId(
                String.format("%s-DES-", envName), UUID.randomUUID().toString());
        LOGGER.info("Creating Disk Encryption Set {}", diskEncryptionSetName);
        DiskEncryptionSetInner createdSet = azureClient.createOrUpdateDiskEncryptionSet(diskEncryptionSetName, encryptionKeyUrl, envName, location,
                desResourceGroupName, sourceVaultId, tags);
        if (!Objects.isNull(createdSet)) {
            return new CreatedDiskEncryptionSet.Builder()
                    .withDiskEncryptionSetId(createdSet.id())
                    .withDiskEncryptionSetPrincipalId(createdSet.identity().principalId())
                    .withDiskEncryptionSetLocation(createdSet.location())
                    .withDiskEncryptionSetName(createdSet.name())
                    .withTags(createdSet.getTags())
                    .withDiskEncryptionSetResourceGroup(desResourceGroupName)
                    .build();
        } else {
            throw new RuntimeException("Failed to create Disk Encryption Set");
        }
    }
}