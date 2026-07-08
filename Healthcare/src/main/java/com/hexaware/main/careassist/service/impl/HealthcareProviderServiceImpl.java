package com.hexaware.main.careassist.service.impl;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.HealthcareProviderDTO;
import com.hexaware.main.careassist.entity.HealthcareProvider;
import com.hexaware.main.careassist.entity.AppUser;
import com.hexaware.main.careassist.exception.ResourceNotFoundException;
import com.hexaware.main.careassist.repository.HealthcareProviderRepository;
import com.hexaware.main.careassist.repository.AppUserRepository;
import com.hexaware.main.careassist.service.IHealthcareProviderService;

@Service
@Transactional
@Slf4j
public class HealthcareProviderServiceImpl implements IHealthcareProviderService {

    @Autowired
    private HealthcareProviderRepository providerRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public HealthcareProviderDTO createProviderProfile(HealthcareProviderDTO dto) {
        HealthcareProvider provider = providerRepository.findTopByAppUserUserIdOrderByProviderIdDesc(dto.getUserId())
                .orElseGet(HealthcareProvider::new);

        if (provider.getProviderId() > 0) {
            log.warn("Provider profile already exists for user id {}. Updating existing provider id {} instead of creating a duplicate.",
                    dto.getUserId(), provider.getProviderId());
        }

        copyDtoToEntity(dto, provider);
        return toDTO(providerRepository.save(provider));
    }

    @Override
    public HealthcareProviderDTO updateProviderProfile(Integer providerId, HealthcareProviderDTO dto) {
        HealthcareProvider provider = getProviderEntity(providerId);
        copyDtoToEntity(dto, provider);
        return toDTO(providerRepository.save(provider));
    }

    @Override
    public HealthcareProviderDTO getProviderById(Integer providerId) {
        return toDTO(getProviderEntity(providerId));
    }

    @Override
    public HealthcareProviderDTO getProviderByUserId(Integer userId) {
        HealthcareProvider provider = providerRepository.findTopByAppUserUserIdOrderByProviderIdDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Healthcare provider profile not found for user id: " + userId));
        return toDTO(provider);
    }

    @Override
    public List<HealthcareProviderDTO> getAllProviders() {
        return providerRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void deleteProvider(Integer providerId) {
        providerRepository.delete(getProviderEntity(providerId));
    }

    private HealthcareProvider getProviderEntity(Integer providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Healthcare provider not found with id: " + providerId));
    }

    private AppUser getUser(Integer userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private void copyDtoToEntity(HealthcareProviderDTO dto, HealthcareProvider provider) {
        provider.setAppUser(getUser(dto.getUserId()));
        provider.setProviderName(dto.getProviderName());
        provider.setSpecialization(dto.getSpecialization());
        provider.setLicenseNumber(dto.getLicenseNumber());
        provider.setAddress(dto.getAddress());
    }

    private HealthcareProviderDTO toDTO(HealthcareProvider provider) {
        HealthcareProviderDTO dto = new HealthcareProviderDTO();
        dto.setProviderId(provider.getProviderId());
        dto.setUserId(provider.getAppUser().getUserId());
        dto.setProviderName(provider.getProviderName());
        dto.setSpecialization(provider.getSpecialization());
        dto.setLicenseNumber(provider.getLicenseNumber());
        dto.setAddress(provider.getAddress());
        return dto;
    }
}
