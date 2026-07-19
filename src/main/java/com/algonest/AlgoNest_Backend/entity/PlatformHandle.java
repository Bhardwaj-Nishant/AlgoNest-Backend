package com.algonest.AlgoNest_Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "platform"})
        }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PlatformHandle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false)
    private String handle;
}