package org.cong.backend.laboratory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "laboratory")
public class Laboratory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(columnDefinition = "DECIMAL(10,2)")
    private Double area;

    @Column
    private Integer capacity;

    @Column(length = 50)
    private String type;

    /**
     * 0-不可用，1-可用，2-维护中
     */
    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}

