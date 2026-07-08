package com.hexaware.main.careassist.service.impl;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.PatientDTO;
import com.hexaware.main.careassist.entity.Patient;
import com.hexaware.main.careassist.entity.AppUser;
import com.hexaware.main.careassist.exception.ResourceNotFoundException;
import com.hexaware.main.careassist.repository.PatientRepository;
import com.hexaware.main.careassist.repository.AppUserRepository;
import com.hexaware.main.careassist.service.IPatientService;

@Service
@Transactional
@Slf4j
public class PatientServiceImpl implements IPatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public PatientDTO createPatientProfile(PatientDTO dto) {
        Patient patient = patientRepository.findTopByAppUserUserIdOrderByPatientIdDesc(dto.getUserId())
                .orElseGet(Patient::new);

        if (patient.getPatientId() > 0) {
            log.warn("Patient profile already exists for user id {}. Updating existing patient id {} instead of creating a duplicate.",
                    dto.getUserId(), patient.getPatientId());
        }

        copyDtoToEntity(dto, patient);
        return toDTO(patientRepository.save(patient));
    }

    @Override
    public PatientDTO updatePatientProfile(Integer patientId, PatientDTO dto) {
        Patient patient = getPatientEntity(patientId);
        copyDtoToEntity(dto, patient);
        return toDTO(patientRepository.save(patient));
    }

    @Override
    public PatientDTO getPatientById(Integer patientId) {
        return toDTO(getPatientEntity(patientId));
    }

    @Override
    public PatientDTO getPatientByUserId(Integer userId) {
        Patient patient = patientRepository.findTopByAppUserUserIdOrderByPatientIdDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user id: " + userId));
        return toDTO(patient);
    }

    @Override
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void deletePatient(Integer patientId) {
        patientRepository.delete(getPatientEntity(patientId));
    }

    private Patient getPatientEntity(Integer patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
    }

    private AppUser getUser(Integer userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private void copyDtoToEntity(PatientDTO dto, Patient patient) {
        patient.setAppUser(getUser(dto.getUserId()));
        patient.setFullName(dto.getFullName());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setAddress(dto.getAddress());
        patient.setSymptoms(dto.getSymptoms());
        patient.setTreatment(dto.getTreatment());
    }

    private PatientDTO toDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setPatientId(patient.getPatientId());
        dto.setUserId(patient.getAppUser().getUserId());
        dto.setFullName(patient.getFullName());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setAddress(patient.getAddress());
        dto.setSymptoms(patient.getSymptoms());
        dto.setTreatment(patient.getTreatment());
        return dto;
    }
}
