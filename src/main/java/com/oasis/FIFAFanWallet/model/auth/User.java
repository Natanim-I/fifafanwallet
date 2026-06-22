package com.oasis.FIFAFanWallet.model.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users")
@Component
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;
    @Column(unique = true)
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String country;
    private boolean enabled = false;
    private String verificationToken;
    private LocalDateTime tokenExpiry;
}
