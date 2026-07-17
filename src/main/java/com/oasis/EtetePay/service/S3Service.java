package com.oasis.EtetePay.service;

import com.oasis.EtetePay.exception.FailedToUploadToS3Exception;
import com.oasis.EtetePay.exception.IllegalArgumentException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/jpg"
    );

    public String uploadKycImage(MultipartFile imageFile, UUID userId, String imageType){
        validateFile(imageFile);

        String extension = getExtension(imageFile.getOriginalFilename());
        String key = String.format(
                "kyc/%s/%s-%s.%s",
                userId,
                imageType,
                UUID.randomUUID(),
                extension
        );

        try{
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(imageFile.getContentType())
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromInputStream(imageFile.getInputStream(), imageFile.getSize()));

            return key;
        }catch (IOException | S3Exception e){
            throw new FailedToUploadToS3Exception("Failed to upload file to S3: ");
        }

    }

    private void validateFile(MultipartFile imageFile){
        if(imageFile.isEmpty()){
            throw new IllegalArgumentException("File can't be empty.");
        }

        if(!ALLOWED_TYPES.contains(imageFile.getContentType())){
            throw new IllegalArgumentException("File type not allowed. Allowed types are: " + ALLOWED_TYPES);
        }

        if(imageFile.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "File size can't exceed 5MB"
            );
        }
    }

    private String getExtension(@Nullable String originalFilename) {
        if(originalFilename == null || !originalFilename.contains(".")){
            throw new IllegalArgumentException("Invalid file name.");
        }

        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }
}
