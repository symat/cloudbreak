package com.sequenceiq.cloudbreak.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloudera.api.swagger.model.ApiClusterTemplateConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mchange.util.DuplicateElementException;
import com.sequenceiq.authorization.resource.AuthorizationResourceType;
import com.sequenceiq.authorization.service.ResourceCrnAndNameProvider;
import com.sequenceiq.cloudbreak.auth.altus.Crn;
import com.sequenceiq.cloudbreak.auth.altus.CrnResourceDescriptor;
import com.sequenceiq.cloudbreak.common.exception.NotFoundException;
import com.sequenceiq.cloudbreak.domain.CustomServiceConfigs;
import com.sequenceiq.cloudbreak.repository.CustomServiceConfigsRepository;

@Service
public class CustomServiceConfigsService implements ResourceCrnAndNameProvider {
    private CustomServiceConfigsRepository customServiceConfigsRepository;

    @Inject
    private ObjectMapper MAPPER;

    @Autowired
    public CustomServiceConfigsService(CustomServiceConfigsRepository customServiceConfigsRepository) {
        this.customServiceConfigsRepository = customServiceConfigsRepository;
    }

    // How does accountId come into this?
    public String createCRN(String accountId) {
        return Crn.builder(CrnResourceDescriptor.CUSTOM_SERVICE_CONFIGS)
                .setAccountId(accountId)
                .setResource(UUID.randomUUID().toString())
                .build()
                .toString();
    }

    public void decorateWithCrn(CustomServiceConfigs customServiceConfigs, String accountId) { //accountId no need as argument
        customServiceConfigs.setResourceCrn(createCRN(accountId));
    }

    public List<CustomServiceConfigs> getAllCustomServiceConfigs() {
        return customServiceConfigsRepository.findAll();
    }

    public Optional<CustomServiceConfigs> getCustomServiceConfigsByCrn(String Crn) {
        Optional<CustomServiceConfigs> customServiceConfigsByCrn = customServiceConfigsRepository.findCustomServiceConfigsByResourceCrn(Crn);
        if (customServiceConfigsByCrn.isEmpty() && Crn!=null) {
            throw new NotFoundException("Custom Configs with Crn " + " does not exist.");
        }
        return customServiceConfigsByCrn;
    }

    public Map<String, List<ApiClusterTemplateConfig>> getCustomServiceConfigsMap(CustomServiceConfigs customServiceConfigs) throws JsonProcessingException {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Map<String, List<ApiClusterTemplateConfig>> serviceMappedToConfigs = new HashMap<>();
        JsonNode serviceConfigsAsJson = MAPPER.readTree(customServiceConfigs.getServiceConfigs());
        serviceConfigsAsJson.forEach(jsonNode -> {
            String serviceName = jsonNode.get("service_name").asText();
            if (serviceMappedToConfigs.get(serviceName) == null) {
                serviceMappedToConfigs.put(serviceName, new ArrayList<>());
            }
            try {
                serviceMappedToConfigs.get(serviceName).add(MAPPER.readValue(jsonNode.toString(), ApiClusterTemplateConfig.class));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        return serviceMappedToConfigs;
    }

    public CustomServiceConfigs getCustomServiceConfigsByName(String name) {
        Optional<CustomServiceConfigs> customServiceConfigsByName = Optional.ofNullable(customServiceConfigsRepository.findCustomServiceConfigsByName(name)
                .orElseThrow(() -> new NotFoundException("Custom configs with name " + name + " does not exist.")));
        return customServiceConfigsByName.get();
    }

    public String addCustomServiceConfigs(CustomServiceConfigs customServiceConfigs, String accountId) {
        //validation
        Optional<CustomServiceConfigs> customServiceConfigsByName =
                customServiceConfigsRepository.findCustomServiceConfigsByName(customServiceConfigs.getCustomConfigsName());
        if (!customServiceConfigsByName.isEmpty()) {
            throw new DuplicateElementException("Custom Configs with name " + customServiceConfigs.getCustomConfigsName() + " exists. Provide a different name.");
        }
        decorateWithCrn(customServiceConfigs, accountId);
        customServiceConfigsRepository.save(customServiceConfigs);
        return customServiceConfigs.getResourceCrn();
    }

    public CustomServiceConfigs deleteCustomServiceConfigsByCrn(String Crn) {
        Optional<CustomServiceConfigs> customServiceConfigsByCrn = customServiceConfigsRepository.findCustomServiceConfigsByResourceCrn(Crn);
        if (customServiceConfigsByCrn.isEmpty()) {
            throw new NotFoundException("Custom Configs with Crn " + Crn + " does not exist.");
        }
        customServiceConfigsRepository.deleteById(customServiceConfigsByCrn.get().getId());
        return customServiceConfigsByCrn.get();
    }

    public CustomServiceConfigs deleteCustomServiceConfigsByName(String name) {
        Optional<CustomServiceConfigs> customServiceConfigsByName = Optional.ofNullable(customServiceConfigsRepository.findCustomServiceConfigsByName(name)
                .orElseThrow(() -> new NotFoundException("Custom Configs with name " + name + " does not exist.")));
        customServiceConfigsRepository.deleteById(customServiceConfigsByName.get().getId());
        return customServiceConfigsByName.get();
    }

    public String updateCustomServiceConfigsByCrn(String Crn, String customServiceConfigsText) {
        Optional<CustomServiceConfigs> customServiceConfigsByCrn = customServiceConfigsRepository.findCustomServiceConfigsByResourceCrn(Crn);
        if (customServiceConfigsByCrn.isEmpty()) {
            throw new NotFoundException("Custom Configs with Crn " + Crn + " does not exist.");
        }
        CustomServiceConfigs customServiceConfigs = customServiceConfigsByCrn.get();
        customServiceConfigs.setServiceConfigs(customServiceConfigsText);
        customServiceConfigs.setLastModified(System.currentTimeMillis());
        customServiceConfigsRepository.save(customServiceConfigs);
        return customServiceConfigs.getResourceCrn();
    }

    public String updateCustomServiceConfigsByName(String name, String customServiceConfigsText) {
        Optional<CustomServiceConfigs> customServiceConfigsByName = Optional.ofNullable(customServiceConfigsRepository.findCustomServiceConfigsByName(name)
                .orElseThrow(() -> new NotFoundException("Custom Configs with name " + name + " does not exist.")));
        CustomServiceConfigs customServiceConfigs = customServiceConfigsByName.get();
        customServiceConfigs.setServiceConfigs(customServiceConfigsText);
        customServiceConfigs.setLastModified(System.currentTimeMillis());
        customServiceConfigsRepository.save(customServiceConfigs);
        return customServiceConfigs.getResourceCrn();
    }

    @Override
    public AuthorizationResourceType getResourceType() {
        return AuthorizationResourceType.CUSTOM_SERVICE_CONFIGS;
    }

    @Override
    public Map<String, Optional<String>> getNamesByCrns(Collection<String> crns) {
        Map<String, Optional<String>> crnMappedToName = new HashMap<>();
        crns.forEach(crn -> crnMappedToName.put(crn, Optional.of(customServiceConfigsRepository.findCustomServiceConfigsByResourceCrn(crn).get().getCustomConfigsName())));
        return crnMappedToName;
    }

    @Override
    public EnumSet<Crn.ResourceType> getCrnTypes() {
        return EnumSet.of(Crn.ResourceType.CUSTOM_SERVICE_CONFIGS);
    }

    @Override
    public String getResourceCrnByResourceName(String resourceName) {
        Optional<CustomServiceConfigs> customServiceConfigsByName = customServiceConfigsRepository.findCustomServiceConfigsByName(resourceName);
        if (customServiceConfigsByName.isEmpty()) {
            throw new NotFoundException("Custom Configs with name " + " does not exist.");
        }
        return customServiceConfigsByName.get().getResourceCrn();
    }

}
