package com.sequenceiq.cloudbreak.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.sequenceiq.authorization.annotation.DisableCheckPermissions;
import com.sequenceiq.cloudbreak.api.endpoint.CustomServiceConfigsEndpoint;
import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.cloudbreak.domain.CustomServiceConfigs;
import com.sequenceiq.cloudbreak.service.CustomServiceConfigsService;

@Controller
public class CustomServiceConfigsController implements CustomServiceConfigsEndpoint {

    private final CustomServiceConfigsService customServiceConfigsService;

    @Autowired
    public CustomServiceConfigsController(CustomServiceConfigsService customServiceConfigsService) {
        this.customServiceConfigsService = customServiceConfigsService;
    }

    @Override
    @DisableCheckPermissions
    public List<CustomServiceConfigs> listCustomServiceConfigs() {
        return customServiceConfigsService.getAllCustomServiceConfigs();
    }

    @Override
    public CustomServiceConfigs listCustomServiceConfigsByCrn(String crn) {
        return customServiceConfigsService.getCustomServiceConfigsByCrn(crn).get();
    }

    @Override
    public CustomServiceConfigs listCustomServiceConfigsByName(String name) {
        return customServiceConfigsService.getCustomServiceConfigsByName(name);
    }

    @Override
    public String addCustomServiceConfigs(CustomServiceConfigs customServiceConfigs) {
        String accountId = ThreadBasedUserCrnProvider.getAccountId();
        return customServiceConfigsService.addCustomServiceConfigs(customServiceConfigs, accountId);
    }

    @Override
    public String updateCustomServiceConfigsByCrn(String Crn, String customServiceConfigsText) {
        return customServiceConfigsService.updateCustomServiceConfigsByCrn(Crn, customServiceConfigsText);
    }

    @Override
    public String updateCustomServiceConfigsByName(String name, String customServiceConfigsText) {
        return customServiceConfigsService.updateCustomServiceConfigsByName(name, customServiceConfigsText);
    }

    @Override
    public CustomServiceConfigs deleteCustomServiceConfigsByCrn(String Crn) {
        return customServiceConfigsService.deleteCustomServiceConfigsByCrn(Crn);
    }

    @Override
    public CustomServiceConfigs deleteCustomServiceConfigsByName(String name) {
        return customServiceConfigsService.deleteCustomServiceConfigsByName(name);
    }
}
