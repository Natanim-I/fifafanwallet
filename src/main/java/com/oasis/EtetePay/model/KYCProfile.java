package com.oasis.EtetePay.model;

import com.oasis.EtetePay.enums.KYCStatus;
import com.oasis.EtetePay.model.auth.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class KYCProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID kycId;

    @OneToOne
    private User user;

    private String faydaNumber;
    private KYCStatus status;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String idFrontImageKey;
    private String idBackImageKey;
    private String selfieImageKey;
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;

}
