package com.pickleball.infrastructure.persistence.repositories;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminJpaRepository extends JpaRepository<AdminJpaRepository.AdminEntity, Long> {
    boolean existsByUserId(Long userId);

    @Entity
    @Table(name = "admins")
    @Getter
    @Setter
    class AdminEntity {
        @Id
        @Column(name = "user_id")
        private Long userId;
    }
}
