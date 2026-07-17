package com.oasis.EtetePay.service;

import com.oasis.EtetePay.dto.KycRequest;
import com.oasis.EtetePay.dto.KycResponse;
import com.oasis.EtetePay.enums.KYCStatus;
import com.oasis.EtetePay.exception.KycProfileNotFoundException;
import com.oasis.EtetePay.exception.UserNotFoundException;
import com.oasis.EtetePay.model.KYCProfile;
import com.oasis.EtetePay.model.auth.User;
import com.oasis.EtetePay.repo.KycProfileRepository;
import com.oasis.EtetePay.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KycService {

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public KycResponse processKyc(KycRequest kycRequest, MultipartFile idFrontImage, MultipartFile idBackImage, MultipartFile selfieImage) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        KYCProfile kycProfile = kycProfileRepository.findByUser(user).orElseThrow(() -> new KycProfileNotFoundException("Kyc profile not found."));

        kycProfile.setFaydaNumber(kycRequest.faydaNumber());
        kycProfile.setFirstName(kycRequest.firstName());
        kycProfile.setMiddleName(kycRequest.middleName());
        kycProfile.setLastName(kycRequest.lastName());
        kycProfile.setDateOfBirth(kycRequest.dateOfBirth());
        kycProfile.setPhoneNumber(kycRequest.phoneNumber());
        kycProfile.setStatus(KYCStatus.PENDING);
        kycProfile.setSubmittedAt(LocalDateTime.now());

        String idFrontImageKey = s3Service.uploadKycImage(idFrontImage, user.getUserId(), "id-front");
        String idBackImageKey = s3Service.uploadKycImage(idBackImage, user.getUserId(), "id-back");
        String selfieImageKey = s3Service.uploadKycImage(selfieImage, user.getUserId(), "selfie");

        kycProfile.setIdFrontImageKey(idFrontImageKey);
        kycProfile.setIdBackImageKey(idBackImageKey);
        kycProfile.setSelfieImageKey(selfieImageKey);

        kycProfileRepository.save(kycProfile);

        return new KycResponse(kycProfile.getKycId(), kycProfile.getStatus(), kycProfile.getSubmittedAt());
    }

    public KycResponse getKycStatus() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found."));
        KYCProfile kycProfile = kycProfileRepository.findByUser(user).orElseThrow(() -> new KycProfileNotFoundException("Kyc profile not found."));

        return new KycResponse(kycProfile.getKycId(), kycProfile.getStatus(), kycProfile.getSubmittedAt());
    }
}
