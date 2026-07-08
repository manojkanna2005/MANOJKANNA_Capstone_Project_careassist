package com.hexaware.main.careassist.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.InvoiceDTO;
import com.hexaware.main.careassist.entity.HealthcareProvider;
import com.hexaware.main.careassist.entity.Invoice;
import com.hexaware.main.careassist.entity.Patient;
import com.hexaware.main.careassist.exception.ResourceNotFoundException;
import com.hexaware.main.careassist.repository.HealthcareProviderRepository;
import com.hexaware.main.careassist.repository.InvoiceRepository;
import com.hexaware.main.careassist.repository.PatientRepository;
import com.hexaware.main.careassist.service.IInvoiceService;

@Service
@Transactional
public class InvoiceServiceImpl implements IInvoiceService {

	@Autowired
	private InvoiceRepository invoiceRepository;
	@Autowired
	private HealthcareProviderRepository providerRepository;
	@Autowired
	private PatientRepository patientRepository;

	@Override
	public InvoiceDTO generateInvoice(InvoiceDTO dto) {
		if (dto.getDueDate().isBefore(dto.getInvoiceDate())) {
			throw new IllegalArgumentException("Due date cannot be before invoice date");
		}
		Invoice invoice = toEntity(dto);
		if (invoice.getCreatedAt() == null) {
			invoice.setCreatedAt(LocalDateTime.now());
		}
		if (invoice.getStatus() == null || invoice.getStatus().isBlank()) {
			invoice.setStatus("PENDING");
		}
		calculateInvoiceTotals(invoice);
		return toDTO(invoiceRepository.save(invoice));
	}

	@Override
	public InvoiceDTO updateInvoice(Integer invoiceId, InvoiceDTO dto) {
		Invoice invoice = getInvoiceEntity(invoiceId);
		if (dto.getDueDate().isBefore(dto.getInvoiceDate())) {
			throw new IllegalArgumentException("Due date cannot be before invoice date");
		}
		invoice.setInvoiceNumber(dto.getInvoiceNumber());
		invoice.setHealthcareProvider(getProvider(dto.getProviderId()));
		invoice.setPatient(getPatient(dto.getPatientId()));
		invoice.setInvoiceDate(dto.getInvoiceDate());
		invoice.setDueDate(dto.getDueDate());
		invoice.setConsultationFee(dto.getConsultationFee());
		invoice.setDiagnosticTestsFee(dto.getDiagnosticTestsFee());
		invoice.setDiagnosticScanFee(dto.getDiagnosticScanFee());
		invoice.setMedicationsFee(dto.getMedicationsFee());
		invoice.setTaxPercentage(dto.getTaxPercentage());
		invoice.setStatus(dto.getStatus());
		invoice.setCreatedAt(dto.getCreatedAt());
		calculateInvoiceTotals(invoice);
		return toDTO(invoiceRepository.save(invoice));
	}

	@Override
	public InvoiceDTO getInvoiceById(Integer invoiceId) {
		return toDTO(getInvoiceEntity(invoiceId));
	}

	@Override
	public List<InvoiceDTO> getInvoicesByPatientId(Integer patientId) {
		return invoiceRepository.findByPatientPatientId(patientId).stream().map(this::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<InvoiceDTO> getInvoicesByProviderId(Integer providerId) {
		return invoiceRepository.findByHealthcareProviderProviderId(providerId).stream().map(this::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public InvoiceDTO markInvoiceAsPaid(Integer invoiceId) {
		Invoice invoice = getInvoiceEntity(invoiceId);
		invoice.setStatus("PAID");
		return toDTO(invoiceRepository.save(invoice));
	}

	@Override
	public InvoiceDTO updateInvoiceStatus(Integer invoiceId, String status) {
		Invoice invoice = getInvoiceEntity(invoiceId);
		invoice.setStatus(status);
		return toDTO(invoiceRepository.save(invoice));
	}

	@Override
	public List<InvoiceDTO> getAllInvoices() {
		return invoiceRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
	}

	@Override
	public void deleteInvoice(Integer invoiceId) {
		invoiceRepository.delete(getInvoiceEntity(invoiceId));
	}

	private Invoice getInvoiceEntity(Integer invoiceId) {
		return invoiceRepository.findById(invoiceId)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
	}

	private HealthcareProvider getProvider(Integer providerId) {
		return providerRepository.findById(providerId).orElseThrow(
				() -> new ResourceNotFoundException("Healthcare provider not found with id: " + providerId));
	}

	private Patient getPatient(Integer patientId) {
		return patientRepository.findById(patientId)
				.orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
	}

	private void calculateInvoiceTotals(Invoice invoice) {
		BigDecimal billAmount = nonNull(invoice.getConsultationFee())
				.add(nonNull(invoice.getDiagnosticTestsFee()))
				.add(nonNull(invoice.getDiagnosticScanFee()))
				.add(nonNull(invoice.getMedicationsFee()));

		BigDecimal taxPercentage = nonNull(invoice.getTaxPercentage());
		BigDecimal taxAmount = billAmount.multiply(taxPercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
		BigDecimal totalAmount = billAmount.add(taxAmount);

		invoice.setTaxAmount(taxAmount);
		invoice.setTotalAmount(totalAmount);
	}

	private BigDecimal nonNull(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private InvoiceDTO toDTO(Invoice invoice) {
		InvoiceDTO dto = new InvoiceDTO();
		dto.setInvoiceId(invoice.getInvoiceId());
		dto.setInvoiceNumber(invoice.getInvoiceNumber());
		dto.setProviderId(invoice.getHealthcareProvider().getProviderId());
		dto.setPatientId(invoice.getPatient().getPatientId());
		dto.setInvoiceDate(invoice.getInvoiceDate());
		dto.setDueDate(invoice.getDueDate());
		dto.setConsultationFee(invoice.getConsultationFee());
		dto.setDiagnosticTestsFee(invoice.getDiagnosticTestsFee());
		dto.setDiagnosticScanFee(invoice.getDiagnosticScanFee());
		dto.setMedicationsFee(invoice.getMedicationsFee());
		dto.setTaxPercentage(invoice.getTaxPercentage());
		dto.setTaxAmount(invoice.getTaxAmount());
		dto.setTotalAmount(invoice.getTotalAmount());
		dto.setStatus(invoice.getStatus());
		dto.setCreatedAt(invoice.getCreatedAt());
		return dto;
	}

	private Invoice toEntity(InvoiceDTO dto) {
		Invoice invoice = new Invoice();
		invoice.setInvoiceId(dto.getInvoiceId());
		invoice.setInvoiceNumber(dto.getInvoiceNumber());
		invoice.setHealthcareProvider(getProvider(dto.getProviderId()));
		invoice.setPatient(getPatient(dto.getPatientId()));
		invoice.setInvoiceDate(dto.getInvoiceDate());
		invoice.setDueDate(dto.getDueDate());
		invoice.setConsultationFee(dto.getConsultationFee());
		invoice.setDiagnosticTestsFee(dto.getDiagnosticTestsFee());
		invoice.setDiagnosticScanFee(dto.getDiagnosticScanFee());
		invoice.setMedicationsFee(dto.getMedicationsFee());
		invoice.setTaxPercentage(dto.getTaxPercentage());
		invoice.setStatus(dto.getStatus());
		invoice.setCreatedAt(dto.getCreatedAt());
		return invoice;
	}
}
