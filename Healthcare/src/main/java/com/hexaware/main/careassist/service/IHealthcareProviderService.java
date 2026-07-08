package com.hexaware.main.careassist.service;

import java.util.List;
import com.hexaware.main.careassist.dto.HealthcareProviderDTO;

public interface IHealthcareProviderService {
    HealthcareProviderDTO createProviderProfile(HealthcareProviderDTO dto);
    HealthcareProviderDTO updateProviderProfile(Integer providerId, HealthcareProviderDTO dto);
    HealthcareProviderDTO getProviderById(Integer providerId);
    HealthcareProviderDTO getProviderByUserId(Integer userId);
    List<HealthcareProviderDTO> getAllProviders();
    void deleteProvider(Integer providerId);
}
