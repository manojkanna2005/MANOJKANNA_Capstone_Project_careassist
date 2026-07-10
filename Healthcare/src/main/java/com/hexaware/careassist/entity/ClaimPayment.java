package com.hexaware.careassist.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "claim_payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_claim_payment_claim", columnNames = "claim_id"),
                @UniqueConstraint(name = "uk_claim_payment_reference", columnNames = "transaction_reference")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClaimPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private int paymentId;

        @OneToOne(optional = true, fetch = FetchType.LAZY)
        @JoinColumn(name = "claim_id", nullable = false)
        @NotFound(action = NotFoundAction.IGNORE)
        private Claim claim;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "payment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_mode", nullable = false, length = 30)
    private String paymentMode;

    @Column(name = "transaction_reference", nullable = false, length = 60)
    private String transactionReference;
}
