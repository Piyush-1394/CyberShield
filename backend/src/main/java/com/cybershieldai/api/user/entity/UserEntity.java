package com.cybershieldai.api.user.entity;

import com.cybershieldai.api.common.Role;
import com.cybershieldai.api.organization.entity.OrganizationEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationEntity organization;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false)
    private String fullName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    private String avatarUrl;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
