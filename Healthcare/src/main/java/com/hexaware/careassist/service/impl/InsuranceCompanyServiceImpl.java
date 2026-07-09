package com.hexaware.careassist.service.impl;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.careassist.dto.InsuranceCompanyDTO;
import com.hexaware.careassist.entity.AppUser;
import com.hexaware.careassist.entity.InsuranceCompany;
import com.hexaware.careassist.exception.ResourceNotFoundException;
import com.hexaware.careassist.repository.AppUserRepository;
import com.hexaware.careassist.repository.InsuranceCompanyRepository;
import com.hexaware.careassist.service.IInsuranceCompanyService;

@Service
@Transactional
@Slf4j
public class InsuranceCompanyServiceImpl implements IInsuranceCompanyService {

    @Autowired
    private InsuranceCompanyRepository companyRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public InsuranceCompanyDTO createCompanyProfile(InsuranceCompanyDTO dto) {
        InsuranceCompany company = companyRepository.findTopByAppUserUserIdOrderByCompanyIdDesc(dto.getUserId())
                .orElseGet(InsuranceCompany::new);

        if (company.getCompanyId() > 0) {
            log.warn("Insurance company profile already exists for user id {}. Updating existing company id {} instead of creating a duplicate.",
                    dto.getUserId(), company.getCompanyId());
        }

        copyDtoToEntity(dto, company);
        return toDTO(companyRepository.save(company));
    }

    @Override
    public InsuranceCompanyDTO updateCompanyProfile(Integer companyId, InsuranceCompanyDTO dto) {
        InsuranceCompany company = getCompanyEntity(companyId);
        copyDtoToEntity(dto, company);
        return toDTO(companyRepository.save(company));
    }

    @Override
    public InsuranceCompanyDTO getCompanyById(Integer companyId) {
        return toDTO(getCompanyEntity(companyId));
    }

    @Override
    public InsuranceCompanyDTO getCompanyByUserId(Integer userId) {
        InsuranceCompany company = companyRepository.findTopByAppUserUserIdOrderByCompanyIdDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company profile not found for user id: " + userId));
        return toDTO(company);
    }

    @Override
    public List<InsuranceCompanyDTO> getAllCompanies() {
        return companyRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void deleteCompany(Integer companyId) {
        companyRepository.delete(getCompanyEntity(companyId));
    }

    private InsuranceCompany getCompanyEntity(Integer companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found with id: " + companyId));
    }

    private AppUser getUser(Integer userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private void copyDtoToEntity(InsuranceCompanyDTO dto, InsuranceCompany company) {
        company.setAppUser(getUser(dto.getUserId()));
        company.setCompanyName(dto.getCompanyName());
        company.setRegistrationNumber(dto.getRegistrationNumber());
        company.setAddress(dto.getAddress());
        company.setContactEmail(dto.getContactEmail());
    }

    private InsuranceCompanyDTO toDTO(InsuranceCompany company) {
        InsuranceCompanyDTO dto = new InsuranceCompanyDTO();
        dto.setCompanyId(company.getCompanyId());
        dto.setUserId(company.getAppUser().getUserId());
        dto.setCompanyName(company.getCompanyName());
        dto.setRegistrationNumber(company.getRegistrationNumber());
        dto.setAddress(company.getAddress());
        dto.setContactEmail(company.getContactEmail());
        return dto;
    }
}
