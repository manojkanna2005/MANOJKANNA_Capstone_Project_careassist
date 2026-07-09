package com.hexaware.careassist.service;

import java.util.List;

import com.hexaware.careassist.dto.InsuranceCompanyDTO;

public interface IInsuranceCompanyService {
    InsuranceCompanyDTO createCompanyProfile(InsuranceCompanyDTO dto);
    InsuranceCompanyDTO updateCompanyProfile(Integer companyId, InsuranceCompanyDTO dto);
    InsuranceCompanyDTO getCompanyById(Integer companyId);
    InsuranceCompanyDTO getCompanyByUserId(Integer userId);
    List<InsuranceCompanyDTO> getAllCompanies();
    void deleteCompany(Integer companyId);
}
