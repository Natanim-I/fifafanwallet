package com.oasis.FIFAFanWallet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users")
public class User {
    @Id
    private UUID userId;
    @Column(unique = true)
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String country;
}
