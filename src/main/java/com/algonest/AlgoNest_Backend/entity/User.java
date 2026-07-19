package com.algonest.AlgoNest_Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_user")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auth_user_id", unique = true, nullable = false)
    private UUID authUserId;

    @Column(unique = true, nullable = false)
    private String email;

    private String displayName;

    @CreationTimestamp
    private LocalDateTime createdAt;
}